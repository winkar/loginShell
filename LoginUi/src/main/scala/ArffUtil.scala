import java.io.{File, PrintWriter}
import java.nio.file.Paths

import org.apache.log4j.Logger

/**
  * Created by winkar on 16-5-5.
  */
class ARFFUtil {
  val LoginXmlDir = "/home/winkar/Documents/github/LoginUI/loginData"
  val NonLoginXmlDir = "/home/winkar/Documents/github/LoginUI/data"
  val log = Logger.getLogger("ARFF")


  def getAllXmlFile(dir: String): Array[String] = {
    for (file <- new File(dir).list().filter(_.endsWith(".xml"))) yield Paths.get(dir, file).toString
  }


  def getLoginData = {
    for (file <- getAllXmlFile(LoginXmlDir)) yield Feature(new File(file).getName.stripSuffix(".xml"), file, isLogin = true)
  }

  def getNonLoginData = {
    for (file <- getAllXmlFile(NonLoginXmlDir)) yield Feature(new File(file).getName.stripSuffix(".xml"), file, isLogin = false)
  }


  def generateARFF() = {
    log.info("Generate start")
    val out = new PrintWriter("data.arff")

    out.println("@relation loginUi")
    out.println()

    out.println("@attribute EditTextCount numeric")
    out.println("@attribute ClickableElementCount numeric")
    out.println("@attribute MaxHierarchyDepth numeric")
    out.println("@attribute HierarchyNodeCount numeric")
//    out.println("@attribute ValidString string")
    out.println("@attribute IsLogin {true, false}")
    out.println()


    out.println("@data")
    getLoginData.foreach(out.println)
    getNonLoginData.foreach(out.println)


    out.close()
    log.info("Generate End")
  }
}
