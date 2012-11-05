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

class RemoteActorSelf extends PartestSuite {
  val checkFile = "actmig-remote-actor-self"
  import org.junit._

  val finished = Promise[Boolean]

  @Test(timeout = 10000)
  def test(): Unit = {
    // Can fail with class cast exception in alive
    val myAkkaActor = ActorDSL.actor(new ActWithStash {
      override def preStart() = {
        alive(2011)
        println("registered")
        finished success true
        context.stop(self)
      }

      def receive = {
        case x: Int =>
      }
    })
    Await.ready(finished.future, 5 seconds)
    assertPartest()
  }

}
