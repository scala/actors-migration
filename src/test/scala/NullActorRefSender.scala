package scala.actors.migration

import scala.actors.PoisonPill
import scala.actors.Actor._
import org.junit._
import Assert._
import scala.concurrent._
import scala.concurrent.duration._
import scala.actors.migration._

class NullActorRefSender extends ActorSuite {

  @Test
  def test(): Unit = {
    val finished = Promise[Boolean]
    // asserts the value of the sender
    val replyActor = ActorDSL.actor(new ActWithStash {
      def receive = {
        case "sender" =>
          assertNotSame("Sender must not be deadLetters.", sender, ActWithStash.deadLettersActor)
        case "no sender" =>
          assertEquals("Sender should be a dead letters actor ref", ActWithStash.deadLettersActor, sender)
        case "finished" =>
          finished success true
      }

      override def postStop() {
        finished success true
      }
    })

    // ActorRef of a Thread actor in the context
    replyActor ! "sender"

    // regular Scala actor have a sender
    ActorDSL.actor {
      actor {
        replyActor ! "sender"
      }
    }

    // the migration actor does not have a sender only if it is explicitly specified
    ActorDSL.actor {
      new ActWithStash {
        override def preStart() = {
          replyActor ! "sender"
          replyActor.!("sender")(self)
          replyActor.!("no sender")(null)
          replyActor ! "finished"
          context.stop(self)
        }
        def receive = { case _ => }
      }
    }

    // finish the test
    Await.ready(finished.future, 20 seconds)

  }

}