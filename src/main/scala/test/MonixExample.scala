package test

import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task

import scala.util.{Failure, Random, Success}

class MonixServiceA {
  private val random = new Random()
  def requestA(requestA: RequestA): Task[ResponseA] = Task.eval {
    if (random.nextInt(10) < 5) {
      ResponseA(requestA.a.reverse)
    } else {
      throw new RuntimeException("DB Failed")
    }
  }
}

class MonixServiceB {
  def requestB(requestB: RequestB): Task[ResponseB] = Task.eval {
    ResponseB(requestB.a.reverse)
  }
}

class MonixServiceConsumer(serviceA: MonixServiceA, serviceB: MonixServiceB) extends StrictLogging {
  import monix.execution.Scheduler.Implicits.global

  def start(): Unit = {
    serviceA
      .requestA(RequestA("Hello there", 10))
      .materialize
      .flatMap {
        case Success(responseA) ⇒
          serviceB.requestB(RequestB(responseA.r, 20))

        case Failure(exception) ⇒
          serviceB.requestB(RequestB("Another string", 20))
      }
      .map { result ⇒
        logger.info(s"got $result")
      }
      .runAsync
  }
}