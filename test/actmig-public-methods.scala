/**
 * NOTE: Code snippets from this test are included in the Actor Migration Guide. In case you change
 * code in these tests prior to the 2.10.0 release please send the notification to @vjovanov.
 */
import scala.collection.mutable.ArrayBuffer
import scala.actors.Actor._
import scala.actors._
import scala.util.continuations._
import java.util.concurrent.{ TimeUnit, CountDownLatch }

object Test {
  val NUMBER_OF_TESTS = 8

  // used for sorting non-deterministic output
  val buff = ArrayBuffer[String]()
  val latch = new CountDownLatch(NUMBER_OF_TESTS)
  val toStop = ArrayBuffer[Actor]()

  def append(v: String) = synchronized {
    buff += v
  }

  def main(args: Array[String]) = {

    val respActor = actor {
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
          case _ => exit()
        }
      }
    }

    toStop += respActor

    respActor ! ("bang")

    val res1 = respActor !? (("bang qmark", 0L))
    append(res1.toString)
    latch.countDown()

    val res2 = respActor !? (5000, ("bang qmark", 1L))
    append(res2.toString)
    latch.countDown()

    // this one should timeout
    val res21 = respActor !? (1, ("bang qmark", 5000L))
    append(res21.toString)
    latch.countDown()

    val fut1 = respActor !! (("bang bang in the future", 0L))
    append(fut1().toString())
    latch.countDown()

    val fut2 = respActor !! (("typed bang bang in the future", 0L), { case x: String => x })
    append(fut2())
    latch.countDown()

    // test reply (back and forth communication)
    {
      val a = actor {
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
    }

    // test forward method
    {
      val a = actor {
        val msg = ("forward from an actor", 0L)
        respActor ! msg
        react {
          case a: String =>
            append(a)
            sender forward ("forward")
        }
      }
    }

    // output
    latch.await(10, TimeUnit.SECONDS)
    if (latch.getCount() > 0) {
      println("Error: Tasks have not finished!!!")
    }
    buff.sorted.foreach(println)
    toStop.foreach(_ ! 'stop)
  }
}
