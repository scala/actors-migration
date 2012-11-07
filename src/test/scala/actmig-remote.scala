/**
 * NOTE: Code snippets from this test are included in the Actor Migration Guide. In case you change
 * code in these tests prior to the 2.10.0 release please send the notification to @vjovanov.
 */
package scala.actors.migration
import scala.actors.Actor._
import scala.actors._
import scala.actors.migration._
import scala.actors.remote._
import scala.actors.remote.RemoteActor._

import java.util.concurrent.{ TimeUnit, CountDownLatch }
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.{ Promise, Await }

/**
 * Test that shows migration of Scala remote actors into Akka.
 */
class Remote extends PartestSuite with ActorSuite {
  val checkFile = "actmig-remote"
  import org.junit._

  @Test
  def testScala(): Unit = {
    val finished, finishedInit = Promise[Boolean]
    val port: Int = 10000 + (Math.random * 10000.0).toInt
    // Snippet showing composition of receives
    // Loop with Condition Snippet - before
    class RActor extends Actor {
      def act {
        alive(port)
        register('myActor, this)
        println("registered")
        finishedInit success true

        var c = true
        loopWhile(c) {
          react {
            case 1 =>
              print("1")
            case x: Int =>
              // do task
              println("do task " + x)
              if (x == 42) {
                println("exit")
                c = false
                finished success true
              }
            case _ => println("here")
          }
        }
      }
    }

    val myActor = new RActor
    myActor.start()
    Await.ready(finishedInit.future, 20 seconds)

    actor {
      val myRemoteActor = select(Node("127.0.0.1", port), 'myActor)
      for (_ <- 0 until 100) myRemoteActor ! 1
      myRemoteActor ! 42
    }

    Await.ready(finished.future, 20 seconds)
    assertPartest()
  }

  @Test
  def testAkka(): Unit = {
    val finished, finishedInit = Promise[Boolean]
    val port: Int = 20000 + (Math.random * 10000.0).toInt
    // Loop with Condition Snippet - migrated
    val myAkkaActor = ActorDSL.actor(new ActWithStash {
      override def preStart() = {
        alive(port)
        registerActorRef('myActorAkka, self)
        println("registered")
        finishedInit success true
      }

      def receive = {
        case 1 =>
          print("1")
        case x: Int =>
          // do task
          println("do task " + x)
          if (x == 42) {
            println("exit")
            finished success true 
          }
      }
    })

    Await.ready(finishedInit.future, 20 seconds)

    val sender = ActorDSL.actor(new ActWithStash {
      override def preStart() = {
        val myRemoteActor = selectActorRef(Node("127.0.0.1", port), 'myActorAkka)

        for (_ <- 0 until 100) myRemoteActor ! 1
        myRemoteActor ! 42
      }

      def receive = { case _ => }
    })

    Await.ready(finished.future, 20 seconds)
    sender ! PoisonPill
    myAkkaActor ! PoisonPill
    assertPartest()
  }

}
