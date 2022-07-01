package a20_akka_streams.a01_101

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.util.ByteString

import java.nio.file.Paths
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Try

object a40_FirstPrinciples {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("FirstPrinciples")

    // Materializer
    //TODO:
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    // -- Source ------------------------------------------------------------------------
    //  1st Type: Type of the element source emits
    //  2nd Type: Is Materialized value: Produces auxiliary value when running the source
    //    Ex: Network source might provide information about about bound port (or) peer's address
    //    Specify 'NotUsed' when no auxiliary information is provided
    //  Serializable
    //    - A source can emit any kind of object as long it is serializable
    val source: Source[Int, NotUsed] = Source(1 to 10)

    //-- Consumer Function (Example 1) -------------------------------------------------
    //  - Used to run a source (Believe useful for testing??)
    //  - ** All consumer function starts with 'run*'
    val done: Future[Done] = source.runForeach(println _)
    //-- How to terminate app on completion of Source? -------------------------------
    implicit val ec: ExecutionContextExecutor = system.dispatcher
    done.onComplete((_: Try[Done]) => system.terminate())

    //-- Consumer Function (Example 2) ---------------------------------------------
    val factorials: Source[BigInt, NotUsed] = source.scan(BigInt(1))((acc, next) => acc * next)
    val done2: Future[IOResult] = factorials.map(num => ByteString(s"$num\n"))
      .runWith(FileIO.toPath(Paths.get("factorials.txt")))
    //implicit val ec: ExecutionContextExecutor = system.dispatcher
    done2.onComplete((_: Try[IOResult]) => system.terminate())

    //-- Sink ---------------------------------------------
    val sink = Sink.foreach[Int](println _)

    //-- Graph ---------------------------------------------
    val graph = source.to(sink)

    //-- Materialized value ----------------------------------------------
    //  - Calling run(),
    //    . On the graph is called as materializing the graph
    //    . Materializes all the components (ie source, flow, sink) in the graph
    //    . Produces a single materialized value (ie **result = NotUsed = graph.run())
    //      ~ It is up to us to decide which of the materialized value we care about
    //        (out of all the components materialized)
    //  - A component can materialize multiple times
    //    . How? when we use the same component in different graphs (or)
    //    . How? different runs = different materializations
    //  - A materialized value can be anything (42, Future, connection, actor, NotUsed, etc...)
    //  - Running a graph allocates the required resources
    //    . Example: thread pools, sockets, connections, etc... which are all transparent to us
    val result: NotUsed = graph.run()

    //-- Source With Flow, Sink with Flow, via ---------------------------------------------
    val flow: Flow[Int, Int, NotUsed] = Flow[Int].map(x => x + 1)
    val sourceWithFlow: Source[Int, NotUsed] = source.via(flow)
    val flowWithSink: Sink[Int, NotUsed] = flow.to(sink)
    sourceWithFlow.to(sink).run()
    source.to(flowWithSink).run()
    // **RECOMMENDED: Is much more intuitive
    source.via(flow).to(sink).run()

    //-- Nulls are not allowed (As per reactive framework) -----------------------------------------
    val illegalSource: Source[String, NotUsed] = Source.single[String](null)
    illegalSource.to(Sink.foreach(println _)).run

    //-- Source types -----------------------------------------
    val finiteSource: Source[Int, NotUsed] = Source.single(1)
    val anotherFiniteSource: Source[Int, NotUsed] = Source(List(1, 2, 3))
    val emptySource: Source[Int, NotUsed] = Source.empty[Int]
    // TODO
    val infiniteSource: Source[Int, NotUsed] = Source(Stream.from(1))
    import scala.concurrent.ExecutionContext.Implicits.global
    // TODO
    val futureSource: Source[Int, NotUsed] = Source.fromFuture(Future(42))

    //-- Sink types ---------------------------------------------------------------
    val boringSink: Sink[Any, Future[Done]] = Sink.ignore
    val foreachSink: Sink[String, Future[Done]] = Sink.foreach[String](println _)
    val headSink: Sink[Int, Future[Int]] = Sink.head[Int] // Retrieves head & closes the stream
    val foldSink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)((a, b) => a + b)

    //-- Flow types ---------------------------------------------------------------
    val mapFlow: Flow[Int, Int, NotUsed] = Flow[Int].map(x => 2 * x)
    val takeFlow: Flow[Int, Int, NotUsed] = Flow[Int].take(5) // Take finite elements & close the stream
    // Also have drop & filter
    // flatMap not allowed, but have substreams

    //-- Syntactic sugars (Operators) ---------------------------------------------------------------
    //Is equivalent to: Source(1 to 10).via(Flow[Int].map(x => x * 2))
    val mapSource: Source[Int, NotUsed] = Source(1 to 10).map(x => x * 2)
    //Is equivalent to: mapSource.to(Sink.foreach[Int](println _)).run()
    mapSource.runForeach(println _)
  }

}
