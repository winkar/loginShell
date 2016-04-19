package com.winkar

import java.io.{File, PrintWriter, StringWriter}
import java.nio.file.Paths
import java.util.Date

import org.apache.log4j.Logger
import com.github.nscala_time.time.Imports._
import org.joda.time.format.DateTimeFormatter

import scala.collection.mutable

trait AppTester {
  val log: Logger = Logger.getLogger(Automator.getClass.getName)
  def startTest(): Unit = {
  }
}



class MultiAppTester(ApkFiles: Seq[String]) extends AppTester {
  var appTested = 0

  val failedAppList = mutable.ListBuffer[String]()
  val loginFoundAppList = mutable.ListBuffer[String]()
  val totalApkCount = ApkFiles.size
  var totalTimeInSeconds = 0

  def this(apkDirectoryRoot: String) {
    this(
      for (apkPath <- new File(apkDirectoryRoot).list) yield Paths.get(apkDirectoryRoot, apkPath).toString
    )
  }

  override def startTest() {
    try {
      for (fullPath <- ApkFiles) {
        try {
            val testStartTime = DateTime.now

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
              appTested += 1
              log.info(String.format("Stop testing apk %s", apkFileName))

            }

            val testEndTime = DateTime.now

            val seconds = org.joda.time.Seconds.secondsBetween(testStartTime, testEndTime).getSeconds
            totalTimeInSeconds += seconds
            val averageTime = totalTimeInSeconds.asInstanceOf[Double]/ appTested

            log.info(s"$seconds seconds used for $apkFileName")
            log.info(s"$averageTime time cost for each apk in average ")
            log.info(s"$appTested/$totalApkCount apks already tested")
            log.info(s"${(totalApkCount - appTested) * averageTime} seconds remained")
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
      log.info(s"$totalApkCount apps in total  $appTested apps tested, failed on ${failedAppList.size} apps, found ${loginFoundAppList.size} login Ui")


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
