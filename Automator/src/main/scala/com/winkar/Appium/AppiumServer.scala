package com.winkar.Appium

import java.io.{FileOutputStream, PrintWriter}

import com.winkar.Automator
import org.apache.log4j.Logger

import scala.io.Source
import scala.sys.process._

/**
  * Created by WinKaR on 16/3/31.
  */
class AppiumServer {

  val log: Logger = Logger.getLogger(Automator.getClass.getName)
  var out: PrintWriter = null

  var server: Process = null

  def start() = {
    log.info("Starting Appium server")
    out = new PrintWriter(new FileOutputStream("log/AppiumServer.log", true), true)
    server = Process("appium").run(ProcessLogger(
      fout = out println,
      ferr = out println
    ))
    checkStatus()
    log.info("Appium Server started")
  }

  def restart() = {
    log.info("Restart Server")
    stop()
    start()
  }

  def getStatus: String = Source.fromURL("http://127.0.0.1:4723/wd/hub/status").mkString

  def checkStatus(): Unit = {
    try {
      log.info(getStatus)
    }
    catch {
      case e: java.net.ConnectException =>
        Thread.sleep(1000)
        checkStatus()
    }
  }

  def stop() = {
    server.destroy()
    log.info("Appium server stopped")
    out.close()
  }

}
