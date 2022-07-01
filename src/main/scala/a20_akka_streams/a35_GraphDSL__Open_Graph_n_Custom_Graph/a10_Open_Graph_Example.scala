package a20_akka_streams.a35_Open_Graph

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL, RunnableGraph, Sink, Source}

import scala.concurrent.Future

object OpenGraphExample extends App {

  implicit val system = ActorSystem("OpenGraphs")
  implicit val materializer = ActorMaterializer()


  /*
    A composite source that concatenates 2 sources
    - emits ALL the elements from the first source
    - then ALL the elements from the second
   */

  val firstSource: Source[Int, NotUsed] = Source(1 to 10)
  val secondSource: Source[Int, NotUsed] = Source(42 to 1000)

  // step 1
  val sourceGraph = Source.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // step 2: declaring components
      val concat: UniformFanInShape[Int, Int] = builder.add(Concat[Int](2))

      // step 3: tying them together
      // NOTE: Either one of the elements in this infix notation should be a Shape
      firstSource ~> concat
      secondSource ~> concat

      // step 4
      SourceShape(concat.out)
    }
  )

  //  sourceGraph.to(Sink.foreach(println)).run()


  /*
    Complex sink
   */
  val sink1: Sink[Int, Future[Done]] = Sink.foreach[Int](x => println(s"Meaningful thing 1: $x"))
  val sink2: Sink[Int, Future[Done]] = Sink.foreach[Int](x => println(s"Meaningful thing 2: $x"))

  // step 1
  val sinkGraph = Sink.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // step 2 - add a broadcast
      val broadcast: UniformFanOutShape[Int, Int] = builder.add(Broadcast[Int](2))

      // step 3 - tie components together
      broadcast ~> sink1
      broadcast ~> sink2

      // step 4 - return the shape
      SinkShape(broadcast.in)
    }
  )

  //  firstSource.to(sinkGraph).run()

  /**
   * Challenge - complex flow?
   * Write your own flow that's composed of two other flows
   * - one that adds 1 to a number
   * - one that does number * 10
   */
  val incrementer: Flow[Int, Int, NotUsed] = Flow[Int].map(_ + 1)
  val multiplier: Flow[Int, Int, NotUsed] = Flow[Int].map(_ * 10)

  // step 1
  val flowGraph: Flow[Int, Int, NotUsed] = Flow.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // everything operates on SHAPES

      // step 2 - define auxiliary SHAPES
      val incrementerShape: FlowShape[Int, Int] = builder.add(incrementer)
      val multiplierShape: FlowShape[Int, Int] = builder.add(multiplier)

      // step 3 - connect the SHAPES
      incrementerShape ~> multiplierShape

      FlowShape(incrementerShape.in, multiplierShape.out) // SHAPE
    } // static graph
  ) // component

  firstSource.via(flowGraph).to(Sink.foreach(println)).run()

  /**
  Exercise: flow from a sink and a source?
   ??: Could not understand this?
   */
  def fromSinkAndSource[A, B](sink: Sink[A, _], source: Source[B, _]): Flow[A, B, _] =
  // step 1
    Flow.fromGraph(
      GraphDSL.create() { implicit builder =>
        // step 2: declare the SHAPES
        val sourceShape: SourceShape[B] = builder.add(source)
        val sinkShape: SinkShape[A] = builder.add(sink)


        // step 3
        //sinkShape ~> sourceShape
        // ^ Not possible

        // step 4 - return the shape
        FlowShape(sinkShape.in, sourceShape.out)
      }
    )

  //val f1 = Flow.fromSinkAndSource(Sink.foreach[String](println), Source(1 to 10))
  //^ Here there is not connection b/w Sink & Source involved.
  //  ** So not possible for Source to send terminatino signal or backpressure signal

  val f2 = Flow.fromSinkAndSourceCoupled(Sink.foreach[String](println), Source(1 to 10))
  //^ Provides a way to couple Sink to Source which is not possible with step 3
  //  Good: Can send termination signal & backpressure signal

}