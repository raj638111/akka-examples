package a20_akka_streams.a30_GraphDSL__Graphs

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, FanInShape2, UniformFanInShape, UniformFanOutShape}
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source, Zip}
import akka.{Done, NotUsed}

import scala.concurrent.Future

object a10_Graph_Example {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("Graph")
    implicit val materializer = ActorMaterializer()

    //-- Example 1: Merge two computations using `Fan out`, `Fan In` ------------------------------------------------------------------
    val input: Source[Int, NotUsed] = Source(1 to 1000)
    val incrementer: Flow[Int, Int, NotUsed] = Flow[Int].map(x => x + 1)
    val multiplier: Flow[Int, Int, NotUsed] = Flow[Int].map(x => x + 10)
    val output: Sink[(Int, Int), Future[Done]] = Sink.foreach[(Int, Int)](println)
    // step 1 - setting up the fundamentals for the graph
    val graph = RunnableGraph.fromGraph(
      GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] => // builder = MUTABLE data structure
        import GraphDSL.Implicits._ // brings some nice operators into scope

        // step 2 - add the necessary components of this graph
        val broadcast: UniformFanOutShape[Int, Int] = builder.add(Broadcast[Int](2)) // fan-out operator
        val zip: FanInShape2[Int, Int, (Int, Int)] = builder.add(Zip[Int, Int]) // fan-in operator

        // step 3 - tying up the components
        input ~> broadcast

        broadcast.out(0) ~> incrementer ~> zip.in0
        broadcast.out(1) ~> multiplier ~> zip.in1

        zip.out ~> output

        // step 4 - return a closed shape
        ClosedShape // FREEZE the builder's shape
        // shape
      } // graph
    )
    graph.run()

    /**
     * exercise 1: feed a source into 2 sinks at the same time (hint: use a broadcast)
     */

    val firstSink = Sink.foreach[Int](x => println(s"First sink: $x"))
    val secondSink = Sink.foreach[Int](x => println(s"Second sink: $x"))

    // step 1
    val sourceToTwoSinksGraph = RunnableGraph.fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        // step 2 - declaring the components
        val broadcast: UniformFanOutShape[Int, Int] = builder.add(Broadcast[Int](2))

        // step 3 - tying up the components
        input ~>  broadcast ~> firstSink  // implicit port numbering
                  broadcast ~> secondSink
        //      broadcast.out(0) ~> firstSink
        //      broadcast.out(1) ~> secondSink

        // step 4
        ClosedShape
      }
    )


    /**
     * exercise 2: balance
     */
    import scala.concurrent.duration._
    val fastSource = input.throttle(5, 1.second)
    val slowSource = input.throttle(2, 1.second)

    val sink1 = Sink.fold[Int, Int](0)((count, _) => {
      println(s"Sink 1 number of elements: $count")
      count + 1
    })

    val sink2 = Sink.fold[Int, Int](0)((count, _) => {
      println(s"Sink 2 number of elements: $count")
      count + 1
    })

    // step 1
    val balanceGraph = RunnableGraph.fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._


        // step 2 -- declare components
        // Merge: Takes data from any one of the input (Note: Does not drop any data)
        //  and pass it to the output
        val merge: UniformFanInShape[Int, Int] = builder.add(Merge[Int](2))
        // Balance: Distribute elements equally through its outputs
        val balance: UniformFanOutShape[Int, Int] = builder.add(Balance[Int](2))


        // step 3 -- tie them up
        fastSource ~> merge ~>  balance ~> sink1
        slowSource ~> merge;    balance ~> sink2

        // step 4
        ClosedShape
      }
    )

    balanceGraph.run()




  }

}
