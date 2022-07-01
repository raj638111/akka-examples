package a20_akka_streams.a35_Open_Graph

import akka.{Done, NotUsed}

import java.util.Date
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, FanInShape2, FanOutShape2, FlowShape, Graph, UniformFanInShape, UniformFanOutShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipWith}

import scala.concurrent.Future

object Open_Graph_Example_2 extends App {

  implicit val system = ActorSystem("MoreOpenGraphs")
  implicit val materializer = ActorMaterializer()

  /*
    Example: Max3 operator
    - 3 inputs of type int
    - the maximum of the 3
   */

  // step 1
  val max3StaticGraph: Graph[UniformFanInShape[Int, Int], NotUsed] = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    // step 2 - define aux SHAPES
    val max1: FanInShape2[Int, Int, Int] = builder.add(ZipWith[Int, Int, Int]((a, b) => Math.max(a, b)))
    val max2: FanInShape2[Int, Int, Int] = builder.add(ZipWith[Int, Int, Int]((a, b) => Math.max(a, b)))

    // step 3
    max1.out ~> max2.in0

    // step 4

    UniformFanInShape(max2.out, max1.in0, max1.in1, max2.in1)
  }

  val source1: Source[Int, NotUsed] = Source(1 to 10)
  val source2: Source[Int, NotUsed] = Source((1 to 10).map(_ => 5))
  val source3: Source[Int, NotUsed] = Source((1 to 10).reverse)

  val maxSink: Sink[Int, Future[Done]] = Sink.foreach[Int](x => println(s"Max is: $x"))

  // step 1
  val max3RunnableGraph: RunnableGraph[NotUsed] = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // step 2 - declare SHAPES
      val max3Shape: UniformFanInShape[Int, Int] = builder.add(max3StaticGraph)

      // step 3 - tie
      source1 ~> max3Shape.in(0)
      source2 ~> max3Shape.in(1)
      source3 ~> max3Shape.in(2)
      max3Shape.out ~> maxSink

      // step 4
      ClosedShape
    }
  )

  //  max3RunnableGraph.run()

  // same for UniformFanOutShape

  /*
    Non-uniform fan out shape

    Processing bank transactions
    Txn suspicious if amount > 10000

    Streams component for txns
    - output1: let the transaction go through
    - output2: suspicious txn ids
   */

  case class Transaction(id: String, source: String, recipient: String, amount: Int, date: Date)

  val transactionSource = Source(List(
    Transaction("5273890572", "Paul", "Jim", 100, new Date),
    Transaction("3578902532", "Daniel", "Jim", 100000, new Date),
    Transaction("5489036033", "Jim", "Alice", 7000, new Date)
  ))

  val bankProcessor = Sink.foreach[Transaction](println)
  val suspiciousAnalysisService = Sink.foreach[String](txnId => println(s"Suspicious transaction ID: $txnId"))

  // step 1
  val suspiciousTxnStaticGraph: Graph[FanOutShape2[Transaction, Transaction, String], NotUsed] = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    // step 2 - define SHAPES
    val broadcast: UniformFanOutShape[Transaction, Transaction] = builder.add(Broadcast[Transaction](2))
    val suspiciousTxnFilter: FlowShape[Transaction, Transaction] = builder.add(Flow[Transaction].filter(txn => txn.amount > 10000))
    val txnIdExtractor: FlowShape[Transaction, String] = builder.add(Flow[Transaction].map[String](txn => txn.id))

    // step 3 - tie SHAPES
    broadcast.out(0) ~> suspiciousTxnFilter ~> txnIdExtractor

    // step 4
    // Note: the `new` keyword vis-a-vis `UniformFanInShape` which is a factory method
    new FanOutShape2(broadcast.in, broadcast.out(1), txnIdExtractor.out)
  }

  // step 1
  val suspiciousTxnRunnableGraph: RunnableGraph[NotUsed] = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // step 2
      val suspiciousTxnShape: FanOutShape2[Transaction, Transaction, String] = builder.add(suspiciousTxnStaticGraph)

      // step 3
      transactionSource ~> suspiciousTxnShape.in
      suspiciousTxnShape.out0 ~> bankProcessor
      suspiciousTxnShape.out1 ~> suspiciousAnalysisService

      // step 4
      ClosedShape
    }
  )

  suspiciousTxnRunnableGraph.run()
}