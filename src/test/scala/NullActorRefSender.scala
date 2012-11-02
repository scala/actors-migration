package scala.actors.migration

import scala.actors.PoisonPill
import scala.actors.Actor._
import org.junit._
import Assert._

class NullActorRefSender {

  @Test
  def test(): Unit = {

    // asserts the value of the sender
    val replyActor = ActorDSL.actor(new ActWithStash {
      def receive = {
        case "sender" =>
          assertNotSame("Sender must not be deadLetters.", sender, ActWithStash.deadLettersActor)
        case "no sender" =>
          assertEquals("Sender should be a dead leeters actor ref", ActWithStash.deadLettersActor, sender)
      }
    })

    // regular scala actors has a sender
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
          context.stop(self)
        }
        def receive = { case _ => }
      }
    }

    // no actorref in the context
    replyActor ! "no sender"

    replyActor ! PoisonPill
  }

}