package com.winkar

import java.io.File
import java.nio.file.Paths

import akka.actor.{Actor, ActorRef, Props}
import com.winkar.Utils.{LogUtils, Timer}
import org.apache.log4j.Logger

import scala.collection.mutable

trait AppTester extends Actor {
  val log: Logger = LogUtils.getLogger
}

case class TraversalTestStart()
case class TraversalTestDone()


class MultiAppTester(ApkFiles: Seq[String]) extends AppTester {
  var appTested = 0
  var appIgnored = 0
  val failedAppList = mutable.ListBuffer[String]()
  val loginFoundAppList = mutable.ListBuffer[String]()
  val totalApkCount = ApkFiles.size
  var totalTimeInSeconds = 0
  val fullTimer = new Timer
  var loginUiFoundAppTimeCost = 0

  def this(apkDirectoryRoot: String) {
    this(
      for (apkPath <- new File(apkDirectoryRoot).list) yield Paths.get(apkDirectoryRoot, apkPath).toString
    )
  }

  val apkFileIterator = ApkFiles.iterator

  def nextApkFile = {
    if (apkFileIterator.hasNext) Some(apkFileIterator.next()) else None
  }


  val travelers = {
    Seq(
      context.actorOf(Props[TravelMonitor])
    )
  }

  var travelerNumber = travelers.size

  var starter: ActorRef = null

  override def receive: Receive = {
    case TraversalTestStart =>
      fullTimer.start
      starter = sender
      travelers foreach {
        _ ! Active
      }
    case NextApk =>
      sender ! StartTravel(nextApkFile)

    case Done =>
      travelerNumber -= 1
      if (travelerNumber == 0) {
        val period = fullTimer.stop
        log.info("Automator test finish")
        log.info(s"${period.getHours} hours ${period  .getMinutes} minutes ${period.getSeconds} seconds cost")

        log.info(s"$totalApkCount apps in total  $appTested apps tested, $appIgnored apps ignored, ${failedAppList.size} apps test failed, found ${loginFoundAppList.size} login Ui")

        if (failedAppList.nonEmpty) {
          log.info("Failed App List")
          failedAppList.foreach(log.info)
        }

        if (loginFoundAppList.nonEmpty) {
          log.info("Login Ui found in Apps: ")
          loginFoundAppList.foreach(log.info)
        }

        starter ! TraversalTestDone()
      }

    case TravelResult(cost, status, pkgName, apkFileName) =>
      appTested += 1
      val seconds = cost.toStandardSeconds.getSeconds
      totalTimeInSeconds += seconds
      val averageTime = totalTimeInSeconds.asInstanceOf[Double]/ appTested

      status match {
        case TravelResult.LoginUiFound =>
          loginFoundAppList.append(apkFileName)
          loginUiFoundAppTimeCost += seconds
        case TravelResult.Fail => failedAppList.append(apkFileName)
        case TravelResult.Complete =>
      }

      val loginUiFoundAppAverageTime = loginUiFoundAppTimeCost.asInstanceOf[Double] / loginFoundAppList.size

      log.info(s"$seconds seconds used for $apkFileName")
      log.info(s"${averageTime.formatted("%.2f")} seconds cost for each apk in average ")
      log.info(s"$appTested/$totalApkCount apks already tested")
      log.info(s"${loginFoundAppList.size} login Ui found")
      log.info(s"${loginUiFoundAppAverageTime.formatted("%.2f")} seconds cost for each apps in which login ui found ")
      log.info(s"${((totalApkCount - appTested) * averageTime).formatted("%.2f")} seconds remained")

  }
}

