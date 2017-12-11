package test

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.scalalogging.StrictLogging

case class RequestA(a: String, b: Int)
case class ResponseA(r: String)
case class ResponseA2(r: String)

case class RequestB(a: String, b: Int)
case class ResponseB(r: String)

case class RequestC(a: String, b: Int)
case class ResponseC(r: String)


object EntryPoint extends StrictLogging {
  def runAkkaExample(): Unit = {
    implicit val as = ActorSystem()

    val serviceA = as.actorOf(Props(new ServiceActorA))
    val serviceB = as.actorOf(Props(new ServiceActorA))
    val consumerProps = as.actorOf(Props(new ServiceConsumer(serviceA, serviceB)))
    consumerProps ! StartConsumer
    Thread.sleep(10000)
  }

  def runMonixExample(): Unit = {
    val serviceA = new MonixServiceA
    val serviceB = new MonixServiceB
    val consumer = new MonixServiceConsumer(serviceA, serviceB)
    consumer.start()
    Thread.sleep(10000)
  }

  def main(args: Array[String]): Unit = {
    logger.info("We start!")
    runAkkaExample()
    //runMonixExample
  }
}
