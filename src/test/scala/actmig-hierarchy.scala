/**
 * NOTE: Code snippets from this test are included in the Actor Migration Guide. In case you change
 * code in these tests prior to the 2.10.0 release please send the notification to @vjovanov.
 */
package scala.actors.migration
import scala.actors._

class Hierarchy extends PartestSuite {
  val checkFile = "actmig-hierarchy"
  import org.junit._

  class ReactorActor extends Reactor[String] {
    def act() {
      var cond = true
      loopWhile(cond) {
        react {
          case x if x == "hello1" => println("hello")
          case "exit" => cond = false
        }
      }
    }
  }

  class ReplyActor extends ReplyReactor {
    def act() {
      var cond = true
      loopWhile(cond) {
        react {
          case "hello" => println("hello")
          case "exit" => cond = false;
        }
      }
    }
  }

  @Test(timeout = 10000)
  def test(): Unit = {
    val reactorActor = new ReactorActor
    val replyActor = new ReplyActor
    reactorActor.start()
    replyActor.start()

    reactorActor ! "hello1"
    replyActor ! "hello"

    reactorActor ! "exit"
    replyActor ! "exit"

    Thread.sleep(1000)
    assertPartest()
  }
}