package a10_akka.a10_Hello_World

import akka.actor.typed.ActorSystem
import org.scalatest.funsuite.AnyFunSuite

class HelloWorldMainTest extends AnyFunSuite {

  test("--"){
    val system: ActorSystem[HelloWorldMain.SayHello] = ActorSystem(HelloWorldMain(), "hello")
    system ! HelloWorldMain.SayHello("World")
    //system ! HelloWorldMain.SayHello("Akka")
  }

}
