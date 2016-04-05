package com.winkar

import org.apache.log4j.Logger

import scala.io.Source
import scala.sys.process._

/**
  * Created by WinKaR on 16/3/31.
  */
class AppiumServer {

  val log: Logger = Logger.getLogger(Automator.getClass.getName)

  log.info("Starting Appium server")
  val server: Process = Process("appium").run(ProcessLogger(
    (o : String) => (),
    (e : String) => ()
  ))
  //TODO : http://127.0.0.1:4723/wd/hub/status 判断是否启动完成


  def getStatus() = Source.fromURL("http://127.0.0.1:4723/wd/hub/status").mkString

  def checkStatus(): Unit = {
    try
      getStatus()
    catch {
      case e: java.net.ConnectException =>
        Thread.sleep(1000)
        checkStatus()
    }
  }

  checkStatus()
  log.info("Appium Server started")

  def stop() = {
    log.info("Appium server stopped")
    server.destroy()
  }

}
