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

object Test {
  val finished = Promise[Boolean]

  def main(args: Array[String]): Unit = {
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

  }

}
