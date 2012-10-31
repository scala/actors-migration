package scala.actors
package migration

import scala.actors._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global



private[actors] class OutputChannelRef(val actor: OutputChannel[Any]) extends ActorRef {

  override private[actors] def ?(message: Any, timeout: Duration): scala.concurrent.Future[Any] =
    throw new UnsupportedOperationException("Output channel does not support ?")

  /**
   * Sends a one-way asynchronous message. E.g. fire-and-forget semantics.
   * <p/>
   *
   * <p/>
   * <pre>
   *   actor ! message
   * </pre>
   * <p/>
   */
  def !(message: Any)(implicit sender: ActorRef = null): Unit =
    if (sender != null)
      actor.send(message, sender.localActor)
    else
      actor ! message

  override def equals(that: Any) =
    that.isInstanceOf[OutputChannelRef] && that.asInstanceOf[OutputChannelRef].actor == this.actor

  private[actors] override def localActor: AbstractActor =
    throw new UnsupportedOperationException("Output channel does not have an instance of the actor.")

  def forward(message: Any): Unit = actor.forward(message)

}

private[actors] class ReactorRef(override val actor: Reactor[Any]) extends OutputChannelRef(actor) {

  /**
   * Forwards the message and passes the original sender actor as the sender.
   * <p/>
   * Works with '!' and '?'.
   */
  override def forward(message: Any) = actor.forward(message)

}

private[actors] final class InternalActorRef(override val actor: InternalActor) extends ReactorRef(actor) {

  /**
   * Sends a message asynchronously, returning a future which may eventually hold the reply.
   */
  override private[actors] def ?(message: Any, timeout: Duration): scala.concurrent.Future[Any] = {
    val dur = if (timeout.isFinite()) timeout.toMillis else (java.lang.Long.MAX_VALUE >> 2)
    val replyPromise = Promise[Any]
    scala.concurrent.future {
      scala.concurrent.blocking {
        actor !? (dur, message)
      } match {
        case Some(x) => replyPromise success x
        case None => replyPromise failure new AskTimeoutException("? operation timed out.")
      }
    }
    replyPromise.future
  }

  override def !(message: Any)(implicit sender: ActorRef = null): Unit =
    if (message == PoisonPill)
      actor.stop('normal)
    else if (sender != null)
      actor.send(message, sender.localActor)
    else
      actor ! message

  private[actors] override def localActor: InternalActor = this.actor
}