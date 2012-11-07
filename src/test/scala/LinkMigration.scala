package scala.actors.migration

import scala.actors._
import scala.actors.Actor._
import org.junit._
import Assert._
import scala.concurrent._
import scala.concurrent.duration._

class LinkMigration extends PartestSuite with ActorSuite {
  val checkFile = "link-migration"

  @Test
  def linkMigration(): Unit = {
    val finished = Promise[Boolean]
    val normalActor = new Actor {
      trapExit = true
      def act() = {
        react {
          case Exit(_, reason) =>
            println("sorry about your " + reason)
            finished success true
        }
      }
    }
    normalActor.start()

    val suicideActor = new Actor {
      def act() = {
        link(normalActor)
        println("life sucks")
        self.exit('suicide)
      }
    }
    suicideActor.start()

    Await.ready(finished.future, 20 seconds)
    assertPartest()
  }

  @Test
  def linkMigrationStep2(): Unit = {
    val finished = Promise[Boolean]
    val normalActor = ActorDSL.actor(new Actor {
      trapExit = true
      def act() = {
        react {
          case Exit(_, reason) =>
            println("sorry about your " + reason)
            finished success true
        }
      }
    })

    val suicideActor = ActorDSL.actor(new Actor {
      def act() = {
        link(normalActor)
        println("life sucks")
        self.exit('suicide)
      }
    })

    Await.ready(finished.future, 20 seconds)
    assertPartest()
  }

  @Test
  def linkMigrationStep4(): Unit = {
    val finished = Promise[Boolean]
    val normalActor = ActorDSL.actor(new ActWithStash {
      def receive = {
        case t @ Terminated(_) =>
          println("sorry about your " + t.reason)
          finished success true
          context.stop(self)
      }
    })

    val suicideActor = ActorDSL.actor(new ActWithStash {
      override def preStart() = {
        link(normalActor)
        println("life sucks")
        exit('suicide)
      }

      def receive = { case _ => }
    })

    Await.ready(finished.future, 20 seconds)
    assertPartest()
  }
}