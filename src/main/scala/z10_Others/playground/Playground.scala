package z10_Others.playground

import akka.actor.ActorSystem

object Playground extends App{

  val actorSystem = ActorSystem("HelloAkka")
  println(actorSystem.name)
}
