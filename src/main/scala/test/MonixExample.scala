package test

import com.typesafe.scalalogging.StrictLogging
import monix.eval.Task

class MonixServiceA {
  def requestA(requestA: RequestA): Task[ResponseA] = Task.eval {
    ResponseA(requestA.a.reverse)
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
      .flatMap { responseA ⇒
        serviceB.requestB(RequestB(responseA.r, 20))
      }
      .map { result ⇒
        logger.info(s"got $result")
      }
      .runAsync
  }
}