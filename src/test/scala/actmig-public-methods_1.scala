/**
 * NOTE: Code snippets from this test are included in the Actor Migration Guide. In case you change
 * code in these tests prior to the 2.10.0 release please send the notification to @vjovanov.
 */
package scala.actors.migration

import scala.collection.mutable.ArrayBuffer
import scala.actors.Actor._
import scala.actors._
import scala.actors.migration._
import scala.util._
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.{ TimeUnit, CountDownLatch }
import scala.concurrent.duration._
import scala.actors.migration.pattern._
import scala.concurrent.ExecutionContext.Implicits.global

/*
 * Test that shows how Scala Actors' public methods are translated to
 */
class PublicMethods1 extends PartestSuite with ActorSuite {
  val checkFile = "actmig-public-methods"
  import org.junit._

  val NUMBER_OF_TESTS = 8

  // used for sorting non-deterministic output
  val buff = ArrayBuffer[String]()
  val latch = new CountDownLatch(NUMBER_OF_TESTS)
  val toStop = ArrayBuffer[ActorRef]()

  def append(v: String) = synchronized {
    buff += v
  }

  @Test
  def test1(): Unit = {

    val respActor = ActorDSL.actor(new Actor {
      def act() = {
        loop {
          react {
            case (x: String, time: Long) =>
              Thread.sleep(time)
              reply(x + " after " + time)
            case "forward" =>
              if (self == sender)
                append("forward succeeded")
              latch.countDown()
            case str: String =>
              append(str)
              latch.countDown()
            case x =>
              exit()
          }
        }
      }
    })

    toStop += respActor

    respActor ! "bang"

    implicit val timeout = Timeout(36500 days)

    {
      implicit val timeout = Timeout(36500 days)
      val msg = ("bang qmark", 0L)
      val res = respActor ? msg
      append(Await.result(res, Duration.Inf).toString)
      latch.countDown()
    }

    {
      val msg = ("bang qmark", 1L)
      val res = {
        val fut = respActor.?(msg)(Timeout(5000 milliseconds))
        val optFut = fut map (Some(_)) recover { case _ => None }
        Await.result(optFut, Duration.Inf)
      }

      append(res.toString)

      latch.countDown()
    }

    {
      val msg = ("bang qmark", 2000L)
      val res = {
        val fut = respActor.?(msg)(Timeout(1 milliseconds))
        val optFut = fut map (Some(_)) recover { case _ => None }
        Await.result(optFut, Duration.Inf)
      }
      append(res.toString)
      latch.countDown()
    }

    {
      val msg = ("bang bang in the future", 0L)
      val res = {
        respActor ? msg
      }

      append(Await.result(res, Duration.Inf).toString)
      latch.countDown()
    }

    {
      val handler: PartialFunction[Any, String] = {
        case x: String => x
      }

      val msg = ("typed bang bang in the future", 0L)
      val res = {
        (respActor ? msg) map handler
      }

      append((Await.result(res, Duration.Inf)).toString)
      latch.countDown()
    }

    // test reply (back and forth communication)
    {
      val a = ActorDSL.actor(new Actor {
        def act() = {
          val msg = ("reply from an actor", 0L)
          respActor ! msg
          receive {
            case a: String =>
              append(a)
              reply(msg)
          }

          react {
            case a: String =>
              append(a)
              latch.countDown()
          }

        }
      })
    }

    // test forward method
    {
      val a = ActorDSL.actor(new Actor {
        def act() = {
          val msg = ("forward from an actor", 0L)
          respActor ! msg
          react {
            case a: String =>
              append(a)
              sender forward ("forward")
          }
        }
      })
    }

    // output
    try
      latch.await(20, TimeUnit.SECONDS)
    finally {
      if (latch.getCount() > 0) {
        println("Error: Tasks have not finished!!!")
      }
      buff.sorted.foreach(println)
      toStop.foreach(_ ! 'stop)
    }
    assertPartest()
  }
}
