package z10_Others.playground

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MultithreadingRecap extends App {

  // Creating threads on JVM
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("I am running in parallel")
  } )
  // Syntax sugar offered by scala
  val aThread2 = new Thread(() => println("I am running in parallel"))
  aThread2.start()
  aThread2.join()

  // Unpredictable (Different runs product different results)
  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("hello")))
  val threadGoodbye = new Thread(() => (1 to 1000).foreach(_ => println("goodbye")))
  threadHello.start()
  threadGoodbye.start()

  // Inter thread communication on JVM: Is done via wait - notify mechanism
  import scala.concurrent.ExecutionContext.Implicits.global
  val future: Future[Int] = Future{
    42
  }
  // call backs
  future.onComplete {
    case Success(42) =>
    case Failure(_) =>
  }
  // Future is a monad ; so we can use map(), flatMap(), filter(), for comprehension etc...
  val aProcessedFuture: Future[Int] = future.map(_ + 1)
  val aFlatFuture: Future[Int] = future.flatMap { value: Int =>
    Future(value + 4)
  }
  // Useful utilities for Future: andThen(), recover()/recoverWith()


  // Promises
  

}
