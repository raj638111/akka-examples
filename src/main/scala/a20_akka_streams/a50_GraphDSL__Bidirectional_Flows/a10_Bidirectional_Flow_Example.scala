package a20_akka_streams.a50_GraphDSL__Bidirectional_Flows

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, BidiShape, ClosedShape, FlowShape, Graph, SinkShape, SourceShape}
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}

object Bidirectional_Flow_Example extends App {

  implicit val system = ActorSystem("BidirectionalFlows")
  implicit val materializer = ActorMaterializer()

  /*
    Example: cryptography
   */
  def encrypt(n: Int)(string: String) = string.map(c => (c + n).toChar)
  def decrypt(n: Int)(string: String) = string.map(c => (c - n).toChar)

  // bidiFlow
  val bidiCryptoStaticGraph: Graph[BidiShape[String, String, String, String], NotUsed] = GraphDSL.create() { implicit builder =>
    val encryptionFlowShape: FlowShape[String, String] = builder.add(Flow[String].map(encrypt(3)))
    val decryptionFlowShape: FlowShape[String, String] = builder.add(Flow[String].map(decrypt(3)))

    //    BidiShape(encryptionFlowShape.in, encryptionFlowShape.out, decryptionFlowShape.in, decryptionFlowShape.out)
    BidiShape.fromFlows(encryptionFlowShape, decryptionFlowShape)
  }

  val unencryptedStrings = List("akka", "is", "awesome", "testing", "bidirectional", "flows")
  val unencryptedSource: Source[String, NotUsed] = Source(unencryptedStrings)
  val encryptedSource: Source[String, NotUsed] = Source(unencryptedStrings.map(encrypt(3)))
  //^ Curried Function

  val cryptoBidiGraph = RunnableGraph.fromGraph(
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val unencryptedSourceShape: SourceShape[String] = builder.add(unencryptedSource)
      val encryptedSourceShape: SourceShape[String] = builder.add(encryptedSource)
      val bidi: BidiShape[String, String, String, String] = builder.add(bidiCryptoStaticGraph)
      val encryptedSinkShape: SinkShape[String] = builder.add(Sink.foreach[String](string => println(s"Encrypted: $string")))
      val decryptedSinkShape: SinkShape[String] = builder.add(Sink.foreach[String](string => println(s"Decrypted: $string")))

      unencryptedSourceShape  ~> bidi.in1   ;   bidi.out1 ~> encryptedSinkShape
      decryptedSinkShape      <~ bidi.out2  ;   bidi.in2  <~ encryptedSourceShape

      ClosedShape
    }
  )

  cryptoBidiGraph.run()

  /*
    - encrypting/decrypting
    - encoding/decoding
    - serializing/deserializing
   */
}
