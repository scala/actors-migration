package scala.actors.migration

import scala.actors._
import scala.actors.Actor._
import org.junit._
import Assert._
import scala.concurrent._
import scala.concurrent.duration._

class LinkMigration extends PartestSuite {
  val checkFile = "link-migration"

  @Test
  def linkMigration(): Unit = {
    val finished = Promise[Boolean]
    val normalActor = actor {
      self.trapExit = true
      react {
        case Exit(_, reason) =>
          println("too bad because of your " + reason)
          finished success true
      }
    }

    val suicideActor = actor {
      self.trapExit = true
      link(normalActor)
      println("life sucks")
      self.exit('suicide)
    }

    Await.ready(finished.future, 5 seconds)
    assertPartest()
  }

  @Test
  def linkMigrationStep2(): Unit = {
    val finished = Promise[Boolean]
    val normalActor = ActorDSL.actor(new Actor {
      def act() = {
        self.trapExit = true
        react {
          case Exit(_, reason) =>
            println("too bad because of your " + reason)
            finished success true
        }
      }
    })

    val suicideActor = ActorDSL.actor(new Actor {
      def act() = {
        self.trapExit = true
        link(normalActor)
        println("life sucks")
        self.exit('suicide)
      }
    })

    Await.ready(finished.future, 5 seconds)
    assertPartest()
  }

  @Test
  def linkMigrationStep4(): Unit = {
    val finished = Promise[Boolean]
    val normalActor = ActorDSL.actor(new ActWithStash {
      def receive = {
        case t @ Terminated(_) =>
          println("too bad because of your " + t.reason)
          finished success true
          context.stop(self)
      }
    })

    // TODO mention that self exit is always true
    val suicideActor = ActorDSL.actor(new ActWithStash {
      override def preStart() = {
        link(normalActor)
        println("life sucks")
        exit('suicide) // TODO mention in the guide
      }

      def receive = { case _ => }
    })

    Await.ready(finished.future, 5 seconds)
    assertPartest()
  }
}