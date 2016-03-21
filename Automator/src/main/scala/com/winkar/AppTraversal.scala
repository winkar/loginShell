package com.winkar

import java.io.{File, IOException}
import java.nio.file.Paths
import java.util.Date

import io.appium.java_client.android.{AndroidElement, AndroidKeyCode}
import org.apache.log4j.Logger
import org.openqa.selenium.{By, WebDriverException}

import scala.collection.mutable.Map

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

  val activities = Map[String, UIActivity]()

  def shouldClick(element: UiElement): Boolean = {
    !elementBlackList.contains(element.text) && !element.clicked
  }

  def updateElements(uIActivity: UIActivity): Unit = {

    uIActivity.addElements(
      appiumAgent.findElements(By.xpath("//*[@clickable='true']"))
        .map(new UiElement(_, uIActivity)))

  }

  def backToPreviousActivity(currentActivity: String): Unit = {
    // 很多Activity无法通过一次Back返回. 需要判断是否成功back
    while (appiumAgent.currentActivity == currentActivity) {
      back
      log.info("Back pressed")
    }
    log.info("Jump to activity " + appiumAgent.currentActivity)
  }

  def traversal(currentActivity: String, depth: Int) {
    // 限制深度遍历最大深度
    if (depth > MAX_DEPTH) {
      log.info("Exceed maximum depth; Back")
      backToPreviousActivity(currentActivity)
      return
    }

    // 将当前activity加入集合

    if (activities.contains(currentActivity)) {
      log.info(s"Activity ${currentActivity} has been visited; Back to previous")
      backToPreviousActivity(currentActivity)
      return
    }


    log.info("Current at " + currentActivity)
    log.info("Current traversal depth is " + depth)
    appiumAgent.takeScreenShot(getLogDir)

    val currentUiActivity = new UIActivity(currentActivity)

    activities.update(currentActivity, currentUiActivity)

    updateElements(currentUiActivity)
    log.info(s"${currentUiActivity.clickableUiElements.size} elements found on Acitivity")

    currentUiActivity.clickableUiElements.values.foreach( element => {
      try {
        if (shouldClick(element)) {
          val formattedElement: String = element.toString
          element.click
          log.info("Clicked " + formattedElement)

          val appActivity = appiumAgent.currentActivity

          if (appActivity != currentActivity) {
            log.info("Jumped to activity " + appiumAgent.currentActivity)

            val currentPackage = appiumAgent.currentPackage
            if (currentPackage != appPackage) {
              log.info("Jumped out of App")
              log.info(s"Current at app ${currentPackage}")
              log.info("Back to previous")
              while (appiumAgent.currentPackage != appPackage) back
            } else
              traversal(appActivity, depth + 1)
          }
        }
      } catch {
        // UI被改变后可能出现原来的元素无法点击的情况. 跳过并加载新的元素
        case e :org.openqa.selenium.NoSuchElementException => updateElements(currentUiActivity)
      }
    })

    if (depth == 0) {
      appiumAgent.closeApp
      return
    }

    backToPreviousActivity(currentActivity)
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
