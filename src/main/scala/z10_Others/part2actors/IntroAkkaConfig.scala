package z10_Others.part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {

  // #1: Inline configuration
  val configString =
    """
      |akka {
      | loglevel = "DEBUG"
      |}
      |""".stripMargin
  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("configurationDemo", ConfigFactory.load(config))
  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }
  val actor = system.actorOf(Props[SimpleLoggingActor])
  actor ! "a message"
}
