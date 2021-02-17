package a10_Obj_Style.a20_Master_Worker

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PreRestart, Signal}
import Worker._

class Worker(context: ActorContext[WorkerMessage]) extends AbstractBehavior[WorkerMessage](context) {

  override def onMessage(message: WorkerMessage): Behavior[WorkerMessage] = {
    println("Hello")
    Behaviors.receive { (context: ActorContext[WorkerMessage], message: WorkerMessage) =>
      message match {
        case SomeMessage =>
          context.system.log.info("Message received by worker")
          Behaviors.same
      }
    }
  }

  /*override def onSignal: PartialFunction[Signal, Behavior[WorkerMessage]] = {
    case PreRestart =>
      context.system.log.info(s"In prestart hook")
      this
    case _ =>
      println("some ...")
      this
  }*/


}

object Worker {

  sealed trait WorkerMessage
  final case object SomeMessage extends WorkerMessage

  def apply(): Behavior[WorkerMessage] = {
    Behaviors.setup { context =>
      context.system.log.info("Starting...")
      new Worker(context)
    }
  }
}

