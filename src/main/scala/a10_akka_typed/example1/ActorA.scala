package a10_akka_typed.example1

import java.time.LocalDateTime

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.util.Random

object ActorA {

  // Messages accepted by ActorA
  case object InstantiateActorB extends Msg
  case class Acknowledgement(actorNo: Int) extends Msg
  case object SendMessageToChildActors extends Msg

  // Behavior 1
  def instantiateChildActors(): Behavior[Msg] = Behaviors.receive{
    case (ctx, InstantiateActorB) =>
      val childActors: List[ActorRef[Msg]] = (0 to 1).toList.map { actorNo =>
        ctx.spawn(ActorB.printMessage(actorNo), s"ActorB_Instance_${actorNo}")
      }
      // Send message
      ctx.self ! SendMessageToChildActors
      sendMessage(childActors)
  }

  // Behavior 2
  def sendMessage(actors: List[ActorRef[Msg]]): Behavior[Msg] = Behaviors.receive{
    case (ctx, SendMessageToChildActors) =>
      Thread.sleep(1000)
      val random = Random.between(0, actors.size)
      val actorB: ActorRef[Msg] = actors(random)
      ctx.log.info(s"ActorA: Sending current time to actorB(${random})")
      actorB ! ActorB.Hello(s"Time is now ${LocalDateTime.now()}", ctx.self)
      Behaviors.same
    case (ctx, Acknowledgement(actorNo)) =>
      ctx.log.info(s"ActorA: Acknowledgement from ActorB(${actorNo})")
      ctx.log.info(s"-------------")
      Thread.sleep(1000)
      val random = Random.between(0, actors.size)
      val actorB: ActorRef[Msg] = actors(random)
      ctx.log.info(s"ActorA: Sending current time to actorB(${random})")
      actorB ! ActorB.Hello(s"Time is now ${LocalDateTime.now()}", ctx.self)
      Behaviors.same
  }

}
