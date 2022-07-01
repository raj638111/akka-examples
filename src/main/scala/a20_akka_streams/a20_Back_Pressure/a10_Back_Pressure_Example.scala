package a20_akka_streams.a20_Back_Pressure

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.duration._

object a10_Back_Pressure_Example {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("BackPressureExample")
    implicit val materializer = ActorMaterializer()
    val fastSource = Source(1 to 1000)
    val slowSink = Sink.foreach[Int]{ x =>
      Thread.sleep(1000)
      println(s"Sink: $x")
    }

    //-- Example 1 (Source, Sink) ---------------------------------------------------------
    // Backpressure still works here irrespective of two actors (because of async boundary) we have
    // Backpressure gets propagated from Sink all the way to Source
    //fastSource.async
    //  .to(slowSink).run()

    //-- Example 2 (Source, Flow, Sink) ---------------------------------------------------------
    //  Note: The default buffer for an element in Akka stream is 16 elements
    // A components reacts to backpressure in following way
    //  1. Try to slow down if possible
    //  2. Buffer elements until there is more demand
    //  3. Drop down elements from the buffer
    //  4. Kill the whole stream
    val simpleFlow = Flow[Int].map{ x =>
      println(s"Incoming: $x")
      x + 1
    }
    //fastSource.async
    //  .via(simpleFlow).async
    //  .to(slowSink)
    //  .run()

    //-- Example 3 (Source, Flow with Buffer Strategy, Sink) ---------------------------------------------------------
    // dropHead: Drop the oldest element in buffer for new one
    val bufferedFlow = simpleFlow.buffer(10, overflowStrategy = OverflowStrategy.dropHead)
    fastSource.async
      .via(bufferedFlow).async
      .to(slowSink)
      .run()

    //-- Example 4 (Manual Throttling) ---------------------------------------
    // Emits atmost 2 element per second
    fastSource.throttle(2, 1.second).runWith(Sink.foreach(println))
  }

}
