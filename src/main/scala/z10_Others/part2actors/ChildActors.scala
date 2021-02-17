package z10_Others.part2actors

import akka.actor.{Actor, ActorRef, Props}

object ChildActors extends App {

  // Actors can create other actors
  //   (by using the actors context)
  object Parent{
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }
  class Parent extends Actor {
    import Parent._
    //var child: ActorRef = null
    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        // Create a child actor here
        val childRef = context.actorOf(Props[Child], name)
        //child = childRef
        context
      case TellChild(message: String) =>
        //if(child != null) child forward message
    }
  }
  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path} I got : $message")
    }
  }

  /*
   Guarding actors
    - Are the top-level actors in parent - child tree hierarchy
    1. /system = system guardian actor (Manages all System actors)
    2. /user = user level guardian actor (Every actor we create using system.actorOf() are owned by this guardian)
    3 / = Is the root guardian actor. Manages both /system & /user guardian

  */

  /*
    Actor Selection: Find actor by a path
    val childSelection: ActorRef = system.actorSelection("/user/parent/child")
      . When the path is not available; and we send a message using that ref. Message will not be delivered. (Dead letters)
    childSelection ! "hi"
   */

  /* Danger!
      NEVER PASS MUTABLE ACTOR STATE OR THE `THIS` REFERENCE OF PARENT TO CHILD ACTORS
   */
}
