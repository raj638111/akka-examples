package a10_akka_typed.example1

import akka.actor.typed.ActorSystem

import scala.concurrent.duration.DurationInt

object Example {

  def main(args: Array[String]): Unit = {
    val system: ActorSystem[Msg] = ActorSystem(ActorA.instantiateChildActors(), "actor_system_example")
    system ! ActorA.InstantiateActorB
    Thread.sleep(5000)
    system.terminate()
  }

}
