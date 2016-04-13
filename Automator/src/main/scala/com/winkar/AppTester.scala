package com.winkar

import java.io.{File, PrintWriter, StringWriter}
import java.nio.file.Paths

import org.apache.log4j.Logger

import scala.collection.mutable

trait AppTester {
  val log: Logger = Logger.getLogger(Automator.getClass.getName)
  def startTest(): Unit = {
  }
}



class MultiAppTester(ApkFiles: Seq[String]) extends AppTester {
  var appCount = 0

  val failedAppList = mutable.ListBuffer[String]()
  val loginFoundAppList = mutable.ListBuffer[String]()


  def this(apkDirectoryRoot: String) {
    this(
      for (apkPath <- new File(apkDirectoryRoot).list) yield Paths.get(apkDirectoryRoot, apkPath).toString
    )
  }

  override def startTest() {
    try {
      for (fullPath <- ApkFiles) {
        try {
            GlobalConfig.currentPackage = AndroidUtils.getPackageName(fullPath)

            val apkFileName = Paths.get(fullPath).getFileName.toString

            if (!GlobalConfig.fast || (GlobalConfig.fast &&
              !new File(Paths.get("log", GlobalConfig.currentPackage, "site.xml").toString).exists())) {

              log.info(String.format("Testing apk %s", apkFileName))
              log.info("Get package Name: " + GlobalConfig.currentPackage)


              val appTraversal: AppTraversal = new AppTraversal(fullPath)
              appTraversal.start() match {
                case TravelResult.Complete =>
                case TravelResult.Fail =>
                  failedAppList.append(apkFileName)
                case TravelResult.LoginUiFound =>
                  loginFoundAppList.append(apkFileName)
              }
              appCount += 1
              log.info(String.format("Stop testing apk %s", apkFileName))
            }


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
      log.info(s"${ApkFiles.size} apps in total  $appCount apps tested, failed on ${failedAppList.size} apps, found ${loginFoundAppList.size} login Ui")


      if (failedAppList.nonEmpty) {
        log.info("Failed App List")
        failedAppList.foreach(log.info)
      }


      if (loginFoundAppList.nonEmpty) {
        log.info("Login Ui found in Apps: ")
        loginFoundAppList.foreach(log.info)
      }
    }
  }
}


class SingleAppTester private[winkar](val apkPath: String) extends AppTester {
  override def startTest() = new MultiAppTester(Seq(apkPath)).startTest()
}
