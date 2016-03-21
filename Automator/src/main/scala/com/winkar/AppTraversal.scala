package com.winkar

import java.io.{File, IOException}
import java.nio.file.Paths
import java.util.Date

import io.appium.java_client.android.{AndroidElement, AndroidKeyCode}
import org.apache.log4j.Logger
import org.openqa.selenium.{By, WebDriverException}


class AppTraversal private[winkar](var appPath: String) {
  private var LOG_DIR: String = null
  private var currentActivity: String = null
  private var appPackage: String = null
  private var appiumAgent: AppiumAgent = null
  val MAX_DEPTH = 4
  val log: Logger = Logger.getLogger(Automator.getClass.getName)
  val elementBlackList = Array("否")


  private def getLogDir: String = LOG_DIR


  private def createLogDir: Boolean = {
    var file: File = null
    LOG_DIR = s"log${File.separator}$appPackage${File.separator}${new Date().toString.replace(' ', '_')}"

    LOG_DIR = Paths.get("log", appPackage, s"${new Date().toString.replace(' ', '_')}").toString

    file = new File(LOG_DIR)
    file.mkdirs
  }

  def traversal(currentActivity: String, depth: Int) {
    if (depth > MAX_DEPTH) {
      return
    }
    log.info("Current at " + currentActivity)
    log.info("Current traversal depth is " + depth)
    appiumAgent.takeScreenShot(getLogDir)

//    val pageSource: String = appiumAgent.driver.getPageSource


    val clickableElements: List[AndroidElement] = appiumAgent.findElements(By.xpath("//*[@clickable='true']"))
    for (element <- clickableElements) {
      if (!elementBlackList.contains(element.getText)) {
        val formattedElement: String = appiumAgent.formatAndroidElement(element)
        element.click
        log.info("Clicked " + formattedElement)


        if (appiumAgent.currentActivity != currentActivity) {
          log.info("Jumped to activity " + appiumAgent.currentActivity)

          val currentPackage = appiumAgent.currentPackage
          if (currentPackage != appPackage) {
            log.info("Jumped out of App")
            log.info(s"Current at app ${currentPackage}")
            log.info("Back to previous")
            while (appiumAgent.currentPackage != appPackage) back
          } else traversal(appiumAgent.currentActivity, depth + 1)
        }
      }
    }
    if (depth == 0) {
      appiumAgent.closeApp
      return
    }
    while (appiumAgent.currentActivity == currentActivity) {
      back
      log.info("Back pressed")
      log.info("Jump to activity " + appiumAgent.currentActivity)
    }
  }

  def back = appiumAgent.pressKeyCode(AndroidKeyCode.BACK)

  @throws[IOException]
  def start {
    appiumAgent = new AppiumAgent(appPath)
    try {
      appPackage = AndroidUtils.getPackageName(appPath)
      log.info("Get package Name: " + appPackage)

      if (!createLogDir) {
        throw new IOException("Directory not created")
      }
      currentActivity = appiumAgent.currentActivity
      log.info("Traversal started")
      traversal(currentActivity, 0)
    }
    catch {
      case e: WebDriverException => e.printStackTrace
      case e: Exception => e.printStackTrace
    } finally {
      appiumAgent.takeScreenShot(getLogDir)
      appiumAgent.removeApp(appPackage)
      appiumAgent.quit
    }
  }
}
