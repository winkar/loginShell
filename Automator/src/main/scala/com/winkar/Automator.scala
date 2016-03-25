/**
  * Created by WinKaR on 16/3/18.
  */

package com.winkar

import org.apache.log4j.Logger
import org.apache.log4j.xml.DOMConfigurator

import scala.xml._

object Automator extends App {
  override def main(args: Array[String]): Unit = {
    val log: Logger = Logger.getLogger(Automator.getClass.getName)

    DOMConfigurator.configureAndWatch("config/log4j.xml")

    val config = XML.loadFile("config/config.xml")

    val apkDirectoryRoot = (config\"ApkDirectoryRoot").text

    val mainTester : AppTester = (config\"Mode").text match {
      case "SingleAppTest" => new SingleAppTester(s"${apkDirectoryRoot}/${(config\"ApkName").text}")
      case "MultiAppTest" => new MultiAppTester(apkDirectoryRoot)
    }


    log.info("Automator test started")
    mainTester.startTest()
  }
}