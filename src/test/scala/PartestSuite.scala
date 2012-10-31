import org.junit._
import Assert._

trait PartestSuite {

  private val buffer: StringBuffer = new StringBuffer

  def println(x: Any): Unit = buffer.append(x.toString).append("\n")

  def assertPartest() {
    val file = scala.io.Source.fromFile(makeFilePath)
    val checkString = file.mkString
    file.close()
    assertEquals("Test output does not match the provided check file.", checkString, buffer.toString)
  }

  val checkFile: String

  def makeFilePath = "src/test/scala/" + checkFile + ".check"

}
