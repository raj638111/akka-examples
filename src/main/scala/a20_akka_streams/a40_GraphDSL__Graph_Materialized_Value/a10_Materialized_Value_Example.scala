package a20_akka_streams.a40_GraphDSL__Graph_Materialized_Value

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, FlowShape, SinkShape, UniformFanOutShape}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Materialized_Value_Example extends App {

  implicit val system = ActorSystem("GraphMaterializedValues")
  implicit val materializer = ActorMaterializer()

  val wordSource: Source[String, NotUsed] = Source(List("Akka", "is", "awesome", "rock", "the", "jvm"))
  val printer: Sink[String, Future[Done]] = Sink.foreach[String](println)
  val counter: Sink[String, Future[Int]] = Sink.fold[Int, String](0)((count, _) => count + 1)

  /*
    A composite component (sink)
    - prints out all strings which are lowercase
    - COUNTS the strings that are short (< 5 chars)
   */

  // step 1
  val complexWordSink: Sink[String, Future[Int]] = Sink.fromGraph(
    // Here in, (printerMatValue, counterMatValue) => counterMatValue)
    //  either I can choose one of them or I can also aggregate both
    // TODO: ??: Thing to learn: Higher order functions
    GraphDSL.create(printer, counter)((printerMatValue, counterMatValue) => counterMatValue) { implicit builder => (printerShape, counterShape) =>
      import GraphDSL.Implicits._

      // step 2 - SHAPES
      val broadcast: UniformFanOutShape[String, String] = builder.add(Broadcast[String](2))
      val lowercaseFilter: FlowShape[String, String] = builder.add(Flow[String].filter(word => word == word.toLowerCase))
      val shortStringFilter: FlowShape[String, String] = builder.add(Flow[String].filter(_.length < 5))

      // step 3 - connections
      broadcast ~> lowercaseFilter ~> printerShape
      broadcast ~> shortStringFilter ~> counterShape

      // step 4 - the shape
      SinkShape(broadcast.in)
    }
  )

  import system.dispatcher
  val shortStringsCountFuture = wordSource.toMat(complexWordSink)(Keep.right).run()
  shortStringsCountFuture.onComplete {
    case Success(count) => println(s"The total number of short strings is: $count")
    case Failure(exception) => println(s"The count of short strings failed: $exception")
  }

  /**
   * Exercise - enhance a flow to return a materialized value
   */
  def enhanceFlow[A, B](flow: Flow[A, B, _]): Flow[A, B, Future[Int]] = {
    val counterSink: Sink[B, Future[Int]] = Sink.fold[Int, B](0)((count, _) => count + 1)

    Flow.fromGraph(
      GraphDSL.create(counterSink) { implicit builder => counterSinkShape =>
        import GraphDSL.Implicits._

        val broadcast: UniformFanOutShape[B, B] = builder.add(Broadcast[B](2))
        val originalFlowShape: FlowShape[A, B] = builder.add(flow)

        originalFlowShape ~> broadcast ~> counterSinkShape

        FlowShape(originalFlowShape.in, broadcast.out(1))
      }
    )
  }

  val simpleSource = Source(1 to 42)
  val simpleFlow = Flow[Int].map(x => x)
  val simpleSink = Sink.ignore

  val enhancedFlowCountFuture = simpleSource.viaMat(enhanceFlow(simpleFlow))(Keep.right).toMat(simpleSink)(Keep.left).run()
  enhancedFlowCountFuture.onComplete {
    case Success(count) => println(s"$count elements went through the enhanced flow")
    case _ => println("Something failed")
  }


  /*
    Hint: use a broadcast and a Sink.fold
   */
}