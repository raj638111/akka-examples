package a10_Obj_Style.a20_Master_Worker

import akka.actor.typed.ActorSystem
import org.scalatest.funsuite.AnyFunSuite


class MasterTest extends AnyFunSuite {

  test("--"){
    val actorSystem = ActorSystem(Master(), "actor_system_pq_to_json")
    actorSystem ! Master.Init(1)
  }

}
