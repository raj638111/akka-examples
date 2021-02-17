package z10_Others.part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLoggingDemo extends App {

  // Akka logging is done with Actors, so logging is ASYNCHRONOUS
  class SimpleActorWithExplicitLogger extends Actor {
    // #1: Explicit logging
    val logger = Logging(context.system, this)

    override def receive: Receive = {
      case message => logger.info(message.toString)
    }
  }

  val system = ActorSystem("LoggingDemo")
  val actor = system.actorOf(Props[SimpleActorWithExplicitLogger], "actor1")
  actor ! "logging a message"

  // #2: Actor Logging
  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
      case message =>
        log.info(message.toString)
    }
  }
  val actor2 = system.actorOf(Props[ActorWithLogging], "actor2")
  actor2 ! "Another message"

}
