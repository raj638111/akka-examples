package z10_Others.part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi" =>
        // Sender reference example
        // ***Reply to a message / return value
        // **sender() contains the reference of the actor that last sent a message to this actor
        //    Note: The tell method ! has an implicit sender
        //    - When the sender of the message is not another actor, sender() references to null, in which case
        //      the message will be not be delivered and is considered a DEAD LETTER**
        //    - When the sender of the message is another actor, sender() will have a reference to that actor and
        //      the message will be delivered to that actor
        context.sender() ! "Hello there"
      case message: String => println(s"$self: Simple actor $message")
      case number: Int => println(s"Simple actor. I have a no $number")
      case SayHiTo(ref: ActorRef) =>
        // Note: ! method has implicit sender parameter. 'this' actor will be passed as the sender
        ref ! "Hi"
      // Forwarding Example
      case WirelessPhoneMessage(content: String, ref: ActorRef) =>
        // When using *forward, I keep the original sender of the wireless phone message, which is a **no sender
        // as this message is send from the main method...and not from another actor
        ref forward (content + "s")
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "SimpleActor")
  // ** Sending messages is always non blocking and asynchronous
  simpleActor ! "hello actor"

  // **
  // 1 - messages can be of any type
  // 2 - Messages must be IMMUTABLE (Not enforced at compile time. But we should enforce this to avoid big issues)
  //   - Messages must be Serializable
  // 3 - Actors have information about their 'context' & about themselves
  //    'context' has attributes like self (this) etc...
  //                                ^^ context.self (or) self
  //simpleActor ! 10


  // ** Actors can reply (like return value) to messages
  val alis = system.actorOf(Props[SimpleActor], "alis")
  val bob = system.actorOf(Props[SimpleActor], "bob")
  case class SayHiTo(ref: ActorRef)
  //alis ! SayHiTo(bob)

  // ** When there is not sender, the reply will go to DEAD letters
  //alis ! "Hi"

  // Actors can forward message to one another
  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alis ! WirelessPhoneMessage("Hi", bob)


  // Ordering of messages for the same actor?
  /*
    - Akka has a thread pool that it shares with actors
    - Each actor has a message handler & a message queue (mailbox)
    - Akka schedules the actor in one of the thread for execution

    . Only one thread operates on an actor at any time
    . Thread will never release an actor in the middle of processing a message
      (ie processing a message is atomic)
    . Message delivery guarantees
      - At most once delivery (??? : What do we do in case the message is not delivered??? )
      - For any sender / receiver pair, the message order is maintained
   */
}
