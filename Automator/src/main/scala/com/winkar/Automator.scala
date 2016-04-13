/**
  * Created by WinKaR on 16/3/18.
  */

package com.winkar

import org.apache.log4j.Logger
import org.apache.log4j.xml.DOMConfigurator
import scopt.OptionParser

import scala.xml.XML

object Automator extends App {



  override def main(args: Array[String]): Unit = {
    val log: Logger = Logger.getLogger(Automator.getClass.getName)
    DOMConfigurator.configureAndWatch("config/log4j.xml")

    val parser = new OptionParser[Configure]("Automator") {
      head("Automator", "0.0.1")

      opt[String]("mode") action {
        (x, c) => c.copy(mode = x)
      } validate {
        x => if (Configure.Modes.contains(x)) success else failure("mode invalid")
      } text s"Test mode for Automator: ${Configure.Modes.mkString(",")}"

      opt[String]("apkFile") valueName "<file>" action {
        (x, c) => c.copy(apkFile = x)
      } text "apk to test"

      opt[String]("apkDirectory") action {
        (x, c) => c.copy(apkDirectory = x)
      } text "directory contains apks to test"

      opt[String]("configFile") valueName "<file>" action {
        (x, c) => c.copy(configFile = x)
      } text "specified config file"

      opt[Seq[String]]("apkFileList") valueName "<apkFile1>,<apkFile2>..." action {
        (x, c) => c.copy(apkFileList = x)
      } text "apks to test"

      opt[Boolean]("fast") action {
        (x, c) => c.copy(fast = x)
      } text "Fast mode(Skip tested apps)"

      help("help") text "prints this usage text"

      checkConfig {
        c =>  {
          if (c.configFile!=null) success
          else {
            if (c.mode == Configure.SingleAppTest) {
              if (c.apkFile!=null) {
                success
              } else {
                failure("apk file not specified")
              }
            } else if (c.mode == Configure.MultiAppTest) {
              if (c.apkDirectory.nonEmpty || c.apkFileList.nonEmpty) {
                success
              } else {
                failure("apk files not specified")
              }
            } else failure("mode invalid")
          }
        }
      }
    }

    parser.parse(args, Configure()) match {
      case Some(configure) =>

        val mainTester : AppTester =  if (configure.configFile != null) {
          val config = XML.loadFile(configure.configFile)

          val apkDirectoryRoot = (config\"apkDirectory").text

          (config\"mode").text match {
            case Configure.SingleAppTest => new SingleAppTester(s"$apkDirectoryRoot/${(config\"apkFile").text}")
            case Configure.MultiAppTest => new MultiAppTester(apkDirectoryRoot)
          }
        } else {
          configure.mode  match {
            case Configure.SingleAppTest => new SingleAppTester(configure.apkFile)
            case Configure.MultiAppTest => new MultiAppTester(configure.apkDirectory)
          }
        }

        GlobalConfig.fast = configure.fast

        log.info("Automator test started")
        val server = new AppiumServer()

        try {
          mainTester.startTest()
        } finally  {
          server.stop()
        }

      case None =>
        log.info("Invalid command line options")
    }
  }
}
