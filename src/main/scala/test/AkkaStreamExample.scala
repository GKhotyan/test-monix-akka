package test

import akka.actor.ActorSystem
import akka.stream.{ActorAttributes, ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.util.{Failure, Random, Success}
import com.typesafe.scalalogging.StrictLogging

object AkkaStreamExample extends StrictLogging {
  private val random = new Random()
  implicit val actorSystem = ActorSystem()
  import actorSystem.dispatcher
  implicit val flowMaterializer = ActorMaterializer()

  //example with recover exception
  def runRecoverExample():Unit = {
    val source = Source.fromIterator(() => retrieveData().iterator)
    val flow1 = Flow.fromFunction[RequestA, ResponseA](r => ResponseA(r.a.reverse)).recover({
      case _: RuntimeException => ResponseA("Another string")
    })
    val flow2 = Flow.fromFunction[RequestB, ResponseB](r => ResponseB(r.a.reverse))

    val flow3 = flow1.map(r => RequestB(r.r, 0)).via(flow2)

    val sink = Sink.foreach[ResponseB](r => println(r.r))

    source.via(flow3).runWith(sink).andThen{
      case _ =>
        actorSystem.terminate()
    }
  }

  //example with recover with retries
  def runRecoverWithRetriesExample():Unit = {
    val source1 = Source.fromIterator(() => retrieveData().iterator)
    val source2 = source1.recoverWithRetries(3, {
      case _: RuntimeException => source1
    })
    val flow1 = Flow.fromFunction[RequestA, ResponseA](r => ResponseA(r.a.reverse))
    val flow2 = Flow.fromFunction[RequestB, ResponseB](r => ResponseB(r.a.reverse))
    val flow3 = flow1.map(r => RequestB(r.r, 0)).via(flow2)

    val sink = Sink.foreach[ResponseB](r => println(r.r))

    source2.via(flow3).runWith(sink).andThen{
      case _ =>
        actorSystem.terminate()
    }
  }

  //example with decider
  def runDeciderExample():Unit = {
    val decider: Supervision.Decider = {
      case _: RuntimeException =>  Supervision.Stop
      case _ => Supervision.Resume
    }

    val source = Source.fromIterator(() => retrieveData().iterator)

    val flow1 = Flow.fromFunction[RequestA, ResponseA](r => ResponseA(r.a.reverse))
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
    val flow2 = Flow.fromFunction[RequestB, ResponseB](r => ResponseB(r.a.reverse))
    val flow3 = flow1.map(r => RequestB(r.r, 0)).via(flow2)

    val sink = Sink.foreach[ResponseB](r => println(r.r))

    source.via(flow3).runWith(sink).andThen{
      case _ =>
        actorSystem.terminate()
    }
  }

  //example with decider
  def runFlowTreeExample():Unit = {
    val source = Source.fromIterator(() => retrieveData().iterator)

    val flow1 = Flow.fromFunction[RequestA, ResponseA](r => ResponseA(r.a.reverse))
    val flow2 = Flow.fromFunction[RequestB, ResponseB](r => ResponseB(r.a.reverse))
    val flow3 = flow1.map(r => RequestB(r.r, 0)).via(flow2)

    val sink = Sink.foreach[ResponseB](r => println(r.r))

    source.via(flow3).runWith(sink).andThen{
      case _ =>
        actorSystem.terminate()
    }
  }

  def retrieveData(): Seq[RequestA] = {
    if (random.nextInt(10) < 5) {
      logger.info("successfully retry")
      List(RequestA("hello", 10))
    } else {
      logger.info("something wrong")
      throw new RuntimeException("something wrong")
    }
  }

  def main(args: Array[String]): Unit = {
    logger.info("We start!")
    runRecoverExample
//    runRecoverWithRetriesExample
//    runDeciderExample
  }

}