package test

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.scalalogging.StrictLogging

object AkkaStreamExample extends App with StrictLogging {
  implicit val actorSystem = ActorSystem()
  import actorSystem.dispatcher
  implicit val flowMaterializer = ActorMaterializer()

  val source = Source.fromIterator(() => retrieveData().iterator)

  val flow1 = Flow.fromFunction[RequestA, ResponseA](r => ResponseA(r.a.reverse))
  val flow2 = Flow.fromFunction[RequestB, ResponseB](r => ResponseB(r.a.reverse))
  val flow3 = flow1.map(r => RequestB(r.r, 0)).via(flow2)

  val sink = Sink.foreach[ResponseB](r => println(r.r))

  source.via(flow3).runWith(sink)

  def retrieveData(): Seq[RequestA] = {
    List(RequestA("hello", 10))
  }

}