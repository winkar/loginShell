package com.winkar.Utils

import java.io.{File, PrintWriter, StringWriter}
import java.nio.file.Paths
import java.util.Date

import com.winkar.Automator
import org.apache.log4j.Logger

/**
  * Created by winkar on 16-4-19.
  */
object LogUtils {
  val log: Logger = Logger.getLogger(Automator.getClass.getName)

  def printException(e: Exception): Unit = {
    val sw = new StringWriter
    e.printStackTrace(new PrintWriter(sw))
    log.warn(sw.toString)
  }

  var packagelogDir = ""
  var screenshotLogDir = ""
  def getLogger = log

  def initLogDirectory(currentPackage: String) = {
    packagelogDir = Paths.get("log", currentPackage).toString
    screenshotLogDir = Paths.get(packagelogDir, new Date().toString.replace(' ', '_')).toString
    val file = new File(screenshotLogDir)
    file.mkdirs()
  }

  def siteXmlPath = Paths.get(packagelogDir, "site.xml").toString
  def dotFilePath = Paths.get(packagelogDir, "site.dot").toString
}
