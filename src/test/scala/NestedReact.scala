package scala.actors.migration

import scala.actors.PoisonPill
import scala.actors.Actor._
import org.junit._
import Assert._
import scala.concurrent.duration._
import scala.concurrent._

class NestedReact extends PartestSuite with ActorSuite {

  final def initActorStates: Unit = synchronized { scala.actors.Actor.State.New }

  val checkFile = "nested-react"

  @Test
  def testNestedReact() = {
    val finished = Promise[Boolean]
    // Snippet showing composition of receives
    // Loop with Condition Snippet - before
    val myActor = actor {
      var c = true
      loopWhile(c) {
        react {
          case x: Int =>
            // do task
            println("do task " + x)
            if (x == 42) {
              c = false
            } else {
              react {
                case y: String =>
                  println("do string " + y)
              }
            }
            println("after react")
            finished.success(true)
        }
      }
    }
    myActor.start()

    myActor ! "I am a String 1"
    myActor ! 1
    myActor ! 2
    myActor ! 3
    myActor ! "I am a String 2"
    myActor ! "I am a String 3"

    myActor ! 42

    Await.ready(finished.future, 20 seconds)
    assertPartest()
  }

  @Test
  def testNestedReactAkka() = {
    val finished = Promise[Boolean]
    // Loop with Condition Snippet - migrated
    val myAkkaActor = ActorDSL.actor(new ActWithStash {

      def receive = ({
        case x: Int =>
          // do task
          println("do task " + x)
          if (x == 42) {
            println("after react")
            finished.success(true)
            context.stop(self)
          } else {
            unstashAll()
            context.become(({
              case y: String =>
                println("do string " + y)
            }: Receive) andThen { x =>
              unstashAll()
              context.unbecome()
            } orElse {
              case x =>
                stash(x)
            })
          }
      }: Receive) orElse {
        case x =>
          stash(x)
      }
    })

    myAkkaActor ! "I am a String 1"
    myAkkaActor ! 1
    myAkkaActor ! 2
    myAkkaActor ! 3
    myAkkaActor ! "I am a String 2"
    myAkkaActor ! "I am a String 3"

    myAkkaActor ! 42

    Await.ready(finished.future, 20 seconds)
    assertPartest()
  }

}