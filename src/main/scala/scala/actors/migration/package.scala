/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2005-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.actors

import scala.actors.remote.{ Node, RemoteActor }
import scala.concurrent.duration.Duration

package object migration {

  import scala.concurrent.ExecutionContext.Implicits.global

  def selectActorRef(node: Node, sym: Symbol): ActorRef = {
    val remoteActor = RemoteActor.select(node, sym)
    new OutputChannelRef(remoteActor) {
      override private[actors] def ?(message: Any, timeout: Duration): scala.concurrent.Future[Any] = {
        val dur = if (timeout.isFinite()) timeout.toMillis else (java.lang.Long.MAX_VALUE >> 2)
        val replyPromise = scala.concurrent.Promise[Any]
        scala.concurrent.future {
          scala.concurrent.blocking {
            remoteActor !? (dur, message)
          } match {
            case Some(x) => replyPromise success x
            case None => replyPromise failure new AskTimeoutException("? operation timed out.")
          }
        }
        replyPromise.future
      }

      override private[actors] def localActor: AbstractActor =
        remoteActor
    }
  }

  def registerActorRef(name: Symbol, a: ActorRef): Unit =
    RemoteActor.internalRegister(name, a.asInstanceOf[InternalActorRef].localActor)

  implicit def actorSender: ActorRef = new InternalActorRef(Actor.self(Scheduler))
}
