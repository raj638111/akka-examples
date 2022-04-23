package a10_akka_typed.example1

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object ActorB {

  // Message accepted by ActorB
  case class Hello(msg: String, replyTo: ActorRef[Msg]) extends Msg

  // Behavior 1
  def printMessage(actorNo: Int): Behavior[Msg] = Behaviors.receive{
    case (ctx, Hello(msg, replyTo)) =>
      ctx.log.info(s"ActorB($actorNo): Message from ActorA = ${msg}")
      replyTo ! ActorA.Acknowledgement(actorNo)
      Behaviors.same
  }

}
