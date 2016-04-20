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
  val out = new PrintWriter(new FileOutputStream("log/AppiumServer.log", true), true)

  log.info("Starting Appium server")
  var server: Process = null

  def start() = {
    server = Process("appium").run(ProcessLogger(
      fout = out println,
      ferr = out println
    ))
  }

  def restart() = {
    stop()
    start()
    checkStatus()
  }

  def getStatus: String = Source.fromURL("http://127.0.0.1:4723/wd/hub/status").mkString

  def checkStatus(): Unit = {
    try
      log.info(getStatus)

    catch {
      case e: java.net.ConnectException =>
        Thread.sleep(1000)
        checkStatus()
    }
  }

  start()
  checkStatus()
  log.info("Appium Server started")

  def stop() = {
    log.info("Appium server stopped")
    server.destroy()
    out.close()
  }

}
