package a20_akka_streams.a70_Integration_with_External_Service

import akka.{Done, NotUsed}

import java.util.Date
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout

import scala.concurrent.Future

/**
 * Provides two examples
 *  1. Integrating external service using Future
 *  2. Integrating external service using Actor (with ask pattern)
 */
object IntegratingWithExternalServices extends App {

  implicit val system = ActorSystem("IntegratingWithExternalServices")
  implicit val materializer = ActorMaterializer()
  //  import system.dispatcher // not recommended in practice for mapAsync
  implicit val dispatcher = system.dispatchers.lookup("dedicated-dispatcher")


  def genericExtService[A, B](element: A): Future[B] = ???

  // example: simplified PagerDuty
  case class PagerEvent(application: String, description: String, date: Date)

  val eventSource: Source[PagerEvent, NotUsed] = Source(List(
    PagerEvent("AkkaInfra", "Infrastructure broke", new Date),
    PagerEvent("FastDataPipeline", "Illegal elements in the data pipeline", new Date),
    PagerEvent("AkkaInfra", "A service stopped responding", new Date),
    PagerEvent("SuperFrontend", "A button doesn't work", new Date)
  ))

  object PagerService {
    private val engineers = List("Daniel", "John", "Lady Gaga")
    private val emails = Map(
      "Daniel" -> "daniel@rockthejvm.com",
      "John" -> "john@rockthejvm.com",
      "Lady Gaga" -> "ladygaga@rtjvm.com"
    )

    def processEvent(pagerEvent: PagerEvent): Future[String] = Future {
      val engineerIndex = (pagerEvent.date.toInstant.getEpochSecond / (24 * 3600)) % engineers.length
      val engineer = engineers(engineerIndex.toInt)
      val engineerEmail = emails(engineer)

      // page the engineer
      println(s"Sending engineer $engineerEmail a high priority notification: $pagerEvent")
      Thread.sleep(1000)

      // return the email that was paged
      engineerEmail
    }
  }

  val infraEvents: Source[PagerEvent, NotUsed] = eventSource.filter(_.application == "AkkaInfra")
  val pagedEngineerEmails: Source[String, NotUsed] = infraEvents.mapAsync(parallelism = 1)(event => PagerService.processEvent(event))
  // Guarantees the relative order of elements (A slow future can slow down results of other
  // futures in order to maintain ordering). use mapAsyncUnordered if ordering is not important
  // Use futures in their own execution context or dispatcher...or else will starve the actor system for threads
  val pagedEmailsSink: Sink[String, Future[Done]] = Sink.foreach[String](email => println(s"Successfully sent notification to $email"))
  // pagedEngineerEmails.to(pagedEmailsSink).run()

  class PagerActor extends Actor with ActorLogging {
    private val engineers = List("Daniel", "John", "Lady Gaga")
    private val emails = Map(
      "Daniel" -> "daniel@rockthejvm.com",
      "John" -> "john@rockthejvm.com",
      "Lady Gaga" -> "ladygaga@rtjvm.com"
    )

    private def processEvent(pagerEvent: PagerEvent) = {
      val engineerIndex = (pagerEvent.date.toInstant.getEpochSecond / (24 * 3600)) % engineers.length
      val engineer = engineers(engineerIndex.toInt)
      val engineerEmail = emails(engineer)

      // page the engineer
      log.info(s"Sending engineer $engineerEmail a high priority notification: $pagerEvent")
      Thread.sleep(1000)

      // return the email that was paged
      engineerEmail
    }

    override def receive: Receive = {
      case pagerEvent: PagerEvent =>
        sender() ! processEvent(pagerEvent)
    }
  }

  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit val timeout = Timeout(3.seconds)
  val pagerActor: ActorRef = system.actorOf(Props[PagerActor], "pagerActor")
  val alternativePagedEngineerEmails: Source[String, NotUsed] = infraEvents.mapAsync(parallelism = 4)(event => (pagerActor ? event).mapTo[String])
  alternativePagedEngineerEmails.to(pagedEmailsSink).run()

  // do not confuse mapAsync with async (ASYNC boundary)
}