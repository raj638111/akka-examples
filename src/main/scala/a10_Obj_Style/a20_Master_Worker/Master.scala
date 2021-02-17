package a10_Obj_Style.a20_Master_Worker

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import Master._

class Master(context: ActorContext[MasterMessage]) extends AbstractBehavior[MasterMessage](context) {

  override def onMessage(message: MasterMessage): Behavior[MasterMessage] = {
    message match {
      case Init(workerCount) =>
        1 to workerCount foreach { no =>
          context.system.log.info(s"Creating worker $no...")
          val actor: ActorRef[Worker.WorkerMessage] = context.spawn(Worker(), s"worker_$no")
          actor ! Worker.SomeMessage
        }
        this
      case Success =>
        this
      case Failure =>
        this
    }
  }


}

object Master {

  sealed trait MasterMessage
  final case class Init(workerCount: Int) extends MasterMessage
  final case object Success extends MasterMessage
  final case object Failure extends MasterMessage

  sealed trait RunMode
  final case class NMessage(messageCount: Integer) extends RunMode
  final case object InfiniteLoop extends RunMode


  def apply(): Behavior[MasterMessage] = {
    Behaviors.setup(context => new Master(context))
  }

}




