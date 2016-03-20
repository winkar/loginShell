package com.winkar

import scala.sys.process._;


object AndroidUtils {
  private def checkOutput(cmd: String): String = cmd.!!


  def getPackageName(apkPath: String): String = apkPath.split("/").last.split("\\.").dropRight(1).mkString(".")


  def getMainActivity(apkPath: String): String = checkOutput(s"aapt dump badging $apkPath " +
    " | awk -F\" \" '/launchable-activity/ {print $2}'  " +
    " |awk -F\"'\" \'/name=/ {print $2}'")

}
