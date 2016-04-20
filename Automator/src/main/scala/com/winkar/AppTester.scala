package com.winkar

import java.io.{File, PrintWriter, StringWriter}
import java.nio.file.Paths

import com.github.nscala_time.time.Imports._
import org.apache.log4j.Logger

import scala.collection.mutable

trait AppTester {
  val log: Logger = LogUtils.getLogger
  def startTest(): Unit = {
  }
}



class MultiAppTester(ApkFiles: Seq[String]) extends AppTester {
  var appTested = 0
  var appIgnored = 0
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
    val fullTestStartTime = DateTime.now
    try {
      for (fullPath <- ApkFiles) {
        try {
            val testStartTime = DateTime.now

            GlobalConfig.currentPackage = AndroidUtils.getPackageName(fullPath)

            val apkFileName = Paths.get(fullPath).getFileName.toString

            if (!GlobalConfig.fast || (GlobalConfig.fast &&
              !new File(Paths.get("log", GlobalConfig.currentPackage, "site.xml").toString).exists())) {
              appTested += 1
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
              log.info(String.format("Stop testing apk %s", apkFileName))

            } else {
              log.info(s"Site.xml found; Ignore $apkFileName")
              appIgnored += 1
            }

            val testEndTime = DateTime.now

            val seconds = org.joda.time.Seconds.secondsBetween(testStartTime, testEndTime).getSeconds
            totalTimeInSeconds += seconds
            val averageTime = totalTimeInSeconds.asInstanceOf[Double]/ appTested

            log.info(s"$seconds seconds used for $apkFileName")
            log.info(s"${averageTime.formatted("%.2f")} time cost for each apk in average ")
            log.info(s"$appTested/$totalApkCount apks already tested")
            log.info(s"${loginFoundAppList.size} login Ui found")
            log.info(s"${((totalApkCount - appTested) * averageTime).formatted("%.2f")} seconds remained")
        } catch {
          case e: org.openqa.selenium.WebDriverException =>
            LogUtils.printException(e)
        }
      }
    } catch  {
      case e: Exception =>
        LogUtils.printException(e)
    } finally {
      val fullTestEndTime = DateTime.now

      val fullTimeCost = fullTestStartTime to fullTestEndTime toPeriod

      log.info("Automator test finish")
      log.info(s"${fullTimeCost.getHours} hours ${fullTimeCost.getMinutes} minutes ${fullTimeCost.getSeconds} seconds cost")


      log.info(s"$totalApkCount apps in total  $appTested apps tested, $appIgnored apps ignored, ${failedAppList.size} apps test failed, found ${loginFoundAppList.size} login Ui")


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
