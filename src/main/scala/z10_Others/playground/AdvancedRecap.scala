package z10_Others.playground

import scala.concurrent.Future

object AdvancedRecap extends App {

  // Partial functions
  // (Only operate on a subset of the given input domain)

  val partitionFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  val pf = (x: Int) => x match {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  val function: (Int => Int) = partitionFunction

  val modifiedList = List(1, 2, 3).map{
    case 1 => 42
    case _ => 0
  }

  // lift(): Turns the partial function to total function from the return value of Int to Option[Int]
  val lifted: Int => Option[Int] = partitionFunction.lift

  // orElse()
  val pfChain = partitionFunction.orElse[Int, Int]{
    case 60 => 9000
  }

  // Type aliases
  // In Akka, we have ReceiveFunction which are type alias of partial function
  type ReceiveFunction = PartialFunction[Any, Unit]

  def receive: ReceiveFunction = {
    case 1 => println("hello")
    case _ => println("Confused")
  }

  // Implicits
  implicit val timeout = 3000
  def setTimeout(f:() => Unit)(implicit timeout: Int) = f()
  setTimeout(() => println("Something")) // Extra parameter is omitted

  // Implicit conversion
  case class Person(name: String){
    def greet = s"name is $name"
  }
  implicit def fromStringToPerson(string: String): Person = Person(string)
  "peter".greet // fromStringToPerson("peter").greet
  implicit class Dog(name: String) {
    def bark = println("bark")
  }
  "Lassie".bark // new Dog("Lassie").bark

  // Organize implicit properly
  // Local scope
  implicit val inverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  List(1, 2, 3).sorted // Returns 3,2,1

  // Imported scope
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future{
    println("Hello future")
  }

  // Order of implicit fetching: Local scope, Imported scope, Companion objects of the type included in the call
  object Person{
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }
  List(Person("Bob"), Person("Alice")).sorted // Will fetch the implicit argument for the sorted method from comp.object
}
