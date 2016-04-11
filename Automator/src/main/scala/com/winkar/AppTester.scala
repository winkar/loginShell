package com.winkar

import java.io.{File, PrintWriter, StringWriter}

import org.apache.log4j.Logger
import scala.collection.mutable

trait AppTester {
  val log: Logger = Logger.getLogger(Automator.getClass.getName)
  def startTest(): Unit = {
  }
}



class MultiAppTester(var apkDirectoryRoot: String) extends AppTester {
  var appCount = 0

  val failedAppList = mutable.ListBuffer[String]()
  val loginFoundAppList = mutable.ListBuffer[String]()


  override def startTest() {
    val apkRoot: File = new File(apkDirectoryRoot)
    try {
      for (path <- apkRoot.list) {
        try {
            log.info(String.format("Testing apk %s", path))
            val appTraversal: AppTraversal = new AppTraversal(apkDirectoryRoot + File.separator + path)
            appTraversal.start() match {
              case TravelResult.Complete =>
              case TravelResult.Fail =>
                failedAppList.append(path)
              case TravelResult.LoginUiFound =>
                loginFoundAppList.append(path)
            }
            appCount += 1
            log.info(String.format("Stop testing apk %s", path))
        } catch {
          case e: org.openqa.selenium.WebDriverException =>
            val sw = new StringWriter
            e.printStackTrace(new PrintWriter(sw))
            log.warn(sw.toString)
        }
      }
    } catch  {
      case e: Exception =>
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        log.warn(sw.toString)
    } finally {
      log.info(s"$appCount apps tested in total, failed on ${failedAppList.size} apps, found ${loginFoundAppList.size} login Ui")

      log.info("Failed App List")
      failedAppList.foreach(log.info)

      log.info("Login Ui found in Apps: ")
      loginFoundAppList.foreach(log.info)
    }
  }
}


class SingleAppTester private[winkar](val apkPath: String) extends AppTester {
  override def startTest(): Unit = {
    try {
      new AppTraversal(apkPath).start()
    } catch  {
      case e: org.openqa.selenium.WebDriverException =>
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        log.warn(sw.toString)
    }
  }
}
