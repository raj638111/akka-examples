package z10_Others.part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsIntro extends App {

  // Part 1: Actor system
  // Is a data structure that controls no of threads under the hood
  //  which is allocated to running Actors
  // Name of the actor system should only contain alphanumeric characters (no space)
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // Part 2: Create actors
  // - Actors are uniquely identified
  // - Messages are asynchronous
  // - Each actor may respond differently
  // - Actors are really encapsulated (No like in OOP where we read their mind)
  // Example: Word count actor
  class WordCountActor extends Actor {
    // Internal data
    var totalWords = 0
    // Behaviour
    //def receive: PartialFunction[Any, Unit] = {
    //      or Use the alias type
    override def receive: Receive = {
      case message: String =>
        println(s"Received $message")
        totalWords += message.split(" ").length
      case msg => println(s"I cannot understand ${msg.toString}")
    }
  }

  // Part 3: Instantiate an Actor (Not possible to use new() to instantiate)
  val wordCounter: ActorRef = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")
  // Part 4: Communicating with actor
  // - Sending message is completely asynchronous
  // Note: The ! method is also known as "tell"
  wordCounter ! "I am learning and its pretty damn cool"
  anotherWordCounter ! "A different message"

  // Example: Instantiate an actor with argument
  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"my name is $name")
      case _ =>
    }
  }
  // Using new inside Props is legal, but discouraged
  val person = actorSystem.actorOf(Props(new Person("bob")))
  person ! "hi"
  // Recommended method: Is to use Companion object for the class and have the factory method take care of using 'new'
  object Person{
    def props(name: String) = Props(new Person(name))
  }
  val p2 = actorSystem.actorOf(Person.props("bob"))

}
