package a20_akka_streams.a10_Materialized_Value

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import scala.util.{Failure, Success}
import scala.concurrent.Future

object a20_Materialized_Value_Example {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("Materialized Streams")
    implicit val materializer = ActorMaterializer()

    //-- Example 1 ----------------------------------------------
    // **Left component is always materialized if we do not explicitly select
    val simpleGraph = Source(1 to 10).to(Sink.foreach(println _))
    // Here NotUsed is a materialized value
    val simpleMaterializedValue: NotUsed = simpleGraph.run()

    //-- Example 2 ----------------------------------------------
    val source: Source[Int, NotUsed] = Source(1 to 10)
    val sink: Sink[Int, Future[Int]] = Sink.reduce[Int]((a, b) => a + b)
    // Here Future[Int] is a materialized value
    val someFuture: Future[Int] = source.runWith(sink)
    import system.dispatcher
    someFuture.onComplete {
      case Success(value) => println(s"Some of all elements is $value")
      case Failure(ex) => println(s"Some error: $ex")
    }

    //-- Choosing materialized value ----------------------------------------------
    // Here we compute a materialized value based on materialized value of two components
    val simpleSource: Source[Int, NotUsed] = Source(1 to 10)
    val simpleFlow: Flow[Int, Int, NotUsed] = Flow[Int].map(x => x + 1)
    val simpleSink: Sink[Int, Future[Done]] = Sink.foreach[Int](println _)
    //simpleSource.viaMat(simpleFlow)((sourceMat, flowMat) => flowMat)
    //    (or)
    // Also available: Keep.both, Keep.right, Keep.none
    val intermediate: Source[Int, NotUsed] = simpleSource.viaMat(simpleFlow)(Keep.right)
    // Further connection can be made and materialized value selected
    val graph: RunnableGraph[Future[Done]] = intermediate.toMat(simpleSink)(Keep.right)
    graph.run().onComplete{
      case Success(value) => println("Good")
      case Failure(ex) => println("Bad")
    }
    // Syntactic sugars
    // Is equivalent to source.to(Sink.reduce)(Keep.right)
    val sum: Future[Int] = Source(1 to 10).runWith(Sink.reduce(_ + _))
    // (or)
    val sum2: Future[Int] = Source(1 to 10).runReduce(_ + _)
    // Backwards
    Sink.foreach[Int](println _).runWith(Source.single(42))
    // Both ways
    val sum3: (NotUsed, Future[Done]) = Flow[Int].map(x => x + 1).runWith(simpleSource, simpleSink)
  }

}
