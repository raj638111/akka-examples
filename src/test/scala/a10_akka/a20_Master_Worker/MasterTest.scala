package a10_akka.a20_Master_Worker

import akka.actor.typed.ActorSystem
import org.scalatest.funsuite.AnyFunSuite


class MasterTest extends AnyFunSuite {

  test("--"){
    val actorSystem = ActorSystem(Master(), "actor_system")
    actorSystem ! Master.Init(1)
    Thread.sleep(1000)
  }

}
