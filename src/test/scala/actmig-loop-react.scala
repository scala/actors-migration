/**
 * NOTE: Code snippets from this test are included in the Actor Migration Guide. In case you change
 * code in these tests prior to the 2.10.0 release please send the notification to @vjovanov.
 */
package scala.actors.migration
import scala.actors.Actor._
import scala.actors._
import scala.actors.migration._
import java.util.concurrent.{ TimeUnit, CountDownLatch }
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.{ Promise, Await }

class LoopReact extends PartestSuite with ActorSuite {
  val checkFile = "actmig-loop-react"
  import org.junit._


  @Test
  def testLoopWithConditionReact() = {
	  val finishedLWCR, finishedLWCR1 = Promise[Boolean]
    // Loop with Condition Snippet - before
    val myActor = actor {
      var c = true
      loopWhile(c) {
        react {
          case x: Int =>
            // do task
            println("do task")
            if (x == 42) {
              c = false
              finishedLWCR1.success(true)
            }
        }
      }
    }

    myActor.start()
    myActor ! 1
    myActor ! 42

    Await.ready(finishedLWCR1.future, 20 seconds)

    // Loop with Condition Snippet - migrated
    val myAkkaActor = ActorDSL.actor(new ActWithStash {

      def receive = {
        case x: Int =>
          // do task
          println("do task")
          if (x == 42) {
            finishedLWCR.success(true)
            context.stop(self)
          }
      }
    })
    myAkkaActor ! 1
    myAkkaActor ! 42
    Await.ready(finishedLWCR.future, 20 seconds)
    assertPartest()
  }

}