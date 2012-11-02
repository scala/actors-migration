/**
 * NOTE: Code snippets from this test are included in the Actor Migration Guide. In case you change
 * code in these tests prior to the 2.10.0 release please send the notification to @vjovanov.
 */
import scala.actors.Actor._
import scala.actors._
import scala.actors.migration._
import scala.actors.remote._
import scala.actors.remote.RemoteActor._

import java.util.concurrent.{ TimeUnit, CountDownLatch }
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.{ Promise, Await }

class Remote extends PartestSuite {
  val checkFile = "actmig-remote"
  import org.junit._

  val finishedScala, finishedAkka = Promise[Boolean]

  @Test(timeout=10000)
  def test(): Unit = {
    // Snippet showing composition of receives
    // Loop with Condition Snippet - before
    class RActor extends Actor {
      def act {
        alive(2014)
        register('myActor, this)
        println("registered")
        var c = true
        loopWhile(c) {
          react {
            case x: Int =>
              // do task
              println("do task")
              if (x == 42) {
                c = false
                finishedScala.success(true)
              }
            case _ => println("here")
          }
        }
      }
    }

    val myActor = new RActor
    myActor.start()

    actor {
      val myRemoteActor = select(Node("127.0.0.1", 2014), 'myActor)
      myRemoteActor ! 42
    }

    Await.ready(finishedScala.future, 5 seconds)

    // Loop with Condition Snippet - migrated
    val myAkkaActor = ActorDSL.actor(new ActWithStash {
      override def preStart() = {
        alive(2013)
        register('myActorAkka, remoteActorFor(this))
        println("registered")
      }

      def receive = {
        case x: Int =>
          // do task
          println("do task")
          if (x == 42) {
            finishedAkka.success(true)
            context.stop(self)
          }
      }
    })

    actor {
      val myRemoteActor = select(Node("127.0.0.1", 2013), 'myActorAkka)
      myRemoteActor ! 42
    }

    Await.ready(finishedAkka.future, 5 seconds)
  }

}
