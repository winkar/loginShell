package com.winkar.Utils

import java.io.{File, PrintWriter, StringWriter}
import java.nio.file.Paths
import java.util.Date

import com.winkar.Automator
import org.apache.log4j.Logger

import scala.xml.XML

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

  // 每个app对应的log 目录
  var packagelogDir = ""

  // 每次测试的log目录
  var caseLogDir = ""
  def getLogger = log

  def initLogDirectory(currentPackage: String) = {
    packagelogDir = Paths.get("log", currentPackage).toString
    caseLogDir = Paths.get(packagelogDir, new Date().toString.replace(' ', '_')).toString
    val file = new File(caseLogDir)
    file.mkdirs()
  }


  def logLayout(view: String, layout: String) = {
    val viewLayoutPath = Paths.get(caseLogDir, s"$view.xml").toString
    val formater = new xml.PrettyPrinter(80, 4)
    val layoutPrinter = new PrintWriter(viewLayoutPath)
    layoutPrinter.append(formater.format(XML.loadString(layout)))
    layoutPrinter.close()
  }

  def siteXmlPath = Paths.get(packagelogDir, "site.xml").toString
  def dotFilePath = Paths.get(packagelogDir, "site.dot").toString
  def caseLogPath = Paths.get(caseLogDir, "log.out").toString
}
