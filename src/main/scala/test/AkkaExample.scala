package test

import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import scala.concurrent.duration._

class ServiceActorA extends Actor with StrictLogging {
  override def receive: Receive = {
    case r: RequestA ⇒
      sender() ! ResponseA(r.a.reverse)
  }
}

class ServiceActorB extends Actor with StrictLogging {
  override def receive: Receive = {
    case r: RequestB ⇒
      sender() ! ResponseB(r.a.reverse)

    case r: RequestC ⇒
      sender() ! ResponseC(r.a.reverse)
  }
}

case object StartConsumer

class ServiceConsumer(serviceA: ActorRef, serviceB: ActorRef) extends Actor with StrictLogging {
  import akka.pattern.{ask, pipe}
  import context._
  var state = "1"

  override def receive: Receive = {
    case StartConsumer ⇒
      logger.info(s"Starting")
      implicit val timeout = Timeout(10.seconds)
      serviceA ? RequestA("Hello there", 10) pipeTo self

    case ra: ResponseA ⇒
      implicit val timeout = Timeout(10.seconds)
      serviceB ? RequestB(ra.r, 20) map {
        case rb: ResponseB ⇒
          logger.info(s"Got ${rb}")
      }
  }
}
