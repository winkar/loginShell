package com.winkar

import java.io.{PrintWriter, StringWriter}

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

  def getLogger = log
}
