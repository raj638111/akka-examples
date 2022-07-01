package a20_akka_streams.a15_Operator_Fusion_n_Async_Boundaries

import akka.NotUsed
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object a20_Operator_Fusion_example {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("OperatorFusion")
    val materializer: ActorMaterializer = ActorMaterializer()
    val simpleSource = Source(1 to 1000)
    val simpleFlow = Flow[Int].map(x => x + 1)
    val simpleFlow2 = Flow[Int].map(x => x * 10)
    val simpleSink = Sink.foreach[Int](println _)

    //-- Operator Fusion --------------------------------------------
    // This graph runs all the components on the same actor which is called as 'Operator(Component) Fusion'
    val res: NotUsed = simpleSource.via(simpleFlow).via(simpleFlow2).to(simpleSink).run()
    // ^^ The Bad of Operator Fusion: Is Similar to all computation running on same actor like below
    class SimpleActor extends Actor {
      override def receive: Receive = {
        case x: Int =>
          // Flow operator
          val x2 = x + 1
          val y = x2 * 10
          // Sink operation
          println(y)
      }
    }
    val simpleActor = system.actorOf(Props[SimpleActor])
    (1 to 1000).foreach(simpleActor ! _)

    //-- Async boundaries --------------------------------------------
    // Can be created using `async` call
    // Is used to break operator fusion
    // **Each async boundary gets a dedicated actor
    // Communication b/w async boundaries is based on message passing b/w actors
    val complexFlow = Flow[Int].map{x =>
      Thread.sleep(1000)
      x + 1
    }
    val complexFlow2 = Flow[Int].map{x =>
      Thread.sleep(1000)
      x * 10
    }
    simpleSource.via(complexFlow).async // Runs on one actor
      .via(complexFlow2).async          // Runs on another actor
      .to(simpleSink)                   // Runs on third actor
      .run()
  }

}
