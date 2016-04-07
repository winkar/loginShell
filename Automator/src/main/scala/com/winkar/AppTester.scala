package com.winkar

import java.io.{File, PrintWriter, StringWriter}

import org.apache.log4j.Logger


trait AppTester {
  val log: Logger = Logger.getLogger(Automator.getClass.getName)
  def startTest(): Unit = {
  }
}


class MultiAppTester(var apkDirectoryRoot: String) extends AppTester {
  val appBlackList = Array("aimoxiu.theme.mx49c81e403f35f52d4cdc6ad2020da3d8.apk",
                            "aimoxiu.theme.mx62fbed7a2d8bc5f11a4a35ae0289a3b3.apk")

  var appCount = 0
  var failCount = 0
  var loginCount = 0

  override def startTest() {
    val apkRoot: File = new File(apkDirectoryRoot)
    try {
      for (path <- apkRoot.list) {
        try {
          if (!appBlackList.contains(path)) {
            log.info(String.format("Testing apk %s", path))
            val appTraversal: AppTraversal = new AppTraversal(apkDirectoryRoot + File.separator + path)
            appTraversal.start() match {
              case TravelResult.Complete =>
              case TravelResult.Fail => failCount += 1
              case TravelResult.LoginUiFound => loginCount +=1
            }
            appCount += 1
            log.info(String.format("Stop testing apk %s", path))
          }
        } catch {
          case e: org.openqa.selenium.WebDriverException => e.printStackTrace()
        }
      }
    } catch  {
      case e: Exception =>
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        log.warn(sw.toString)
    } finally {
      log.info(s"$appCount apps tested in total, failed on $failCount apps, found $loginCount login Ui")
    }
  }
}


class SingleAppTester private[winkar](val apkPath: String) extends AppTester {
  override def startTest(): Unit = {
    try {
      new AppTraversal(apkPath).start()
    } catch  {
      case e: org.openqa.selenium.WebDriverException => e.printStackTrace()
    }
  }
}
