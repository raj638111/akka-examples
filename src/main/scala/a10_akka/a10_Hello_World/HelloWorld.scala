package a10_akka.a10_Hello_World

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object HelloWorld {

  final case class Greet(whom: String, replyTo: ActorRef[Greeted])
  final case class Greeted(whom: String, from: ActorRef[Greet])

  def apply(): Behavior[Greet] = Behaviors.receive { (context, message: Greet) =>
    context.log.info("Hello {}!", message.whom)
    message.replyTo ! Greeted(message.whom, context.self)
    Behaviors.same
  }

}

/**

Scribbling
def test[T](): Unit = {
  Class[T] match {
  case Int.getClass => println("This is int")
  case Float.getClass => println("This is float")
}
}

*/
