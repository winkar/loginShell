package com.winkar

import java.io.{File, IOException}

import org.apache.log4j.Logger

trait AppTester {
  val log: Logger = Logger.getLogger(Automator.getClass.getName)
  def startTest(): Unit = {
  }
}


class MultiAppTester(var apkDirectoryRoot: String) extends AppTester {
  val appBlackList = Array("aimoxiu.theme.mx49c81e403f35f52d4cdc6ad2020da3d8.apk",
                            "aimoxiu.theme.mx62fbed7a2d8bc5f11a4a35ae0289a3b3.apk")

  override def startTest {
    val apkRoot: File = new File(apkDirectoryRoot)
    for (path <- apkRoot.list) {
      try {
        if (!appBlackList.contains(path)) {
          log.info(String.format("Testing apk %s", path))
          val appTraversal: AppTraversal = new AppTraversal(apkDirectoryRoot + File.separator + path)
          appTraversal.start
          log.info(String.format("Stop testing apk %s", path))
        }
      } catch {
        case e: org.openqa.selenium.WebDriverException => e.printStackTrace
      }
    }
  }
}


class SingleAppTester private[winkar](val apkPath: String) extends AppTester {
  override def startTest: Unit = {
    try {
      new AppTraversal(apkPath).start
    } catch  {
      case e: org.openqa.selenium.WebDriverException => e.printStackTrace
    }
  }
}
