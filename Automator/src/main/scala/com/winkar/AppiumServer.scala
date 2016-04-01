package com.winkar

import org.apache.log4j.Logger

import scala.sys.process._

/**
  * Created by WinKaR on 16/3/31.
  */
class AppiumServer {

  val log: Logger = Logger.getLogger(Automator.getClass.getName)

  log.info("Appium server started")
  val server: Process = Process("appium").run(ProcessLogger(
    (o : String) => (),
    (e : String) => ()
  ))


  Thread.sleep(5000)


  def stop() = {
    log.info("Appium server stopped")
    server.destroy()
  }

}
