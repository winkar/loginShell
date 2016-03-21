/**
  * Created by WinKaR on 16/3/18.
  */

package com.winkar

import org.apache.log4j.Logger
import org.apache.log4j.xml.DOMConfigurator



object Automator extends App {
  override def main(args: Array[String]): Unit = {
    val log: Logger = Logger.getLogger(Automator.getClass.getName)
    val apkDirectoryPath: String = "/Users/WinKaR/Documents/lab/loginShell/apkCrawler/download/full"
    val apkPath: String = "/Users/WinKaR/Documents/lab/loginShell/apk/com.taobao.taobao.apk"

    DOMConfigurator.configureAndWatch("config/log4j.xml")
    log.info("Automator started")
    new SingleAppTester(apkPath).startTest
  }
}