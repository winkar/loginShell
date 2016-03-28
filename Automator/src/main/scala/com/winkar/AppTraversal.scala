package com.winkar

import java.io.{File, IOException}
import java.nio.file.Paths
import java.util.Date

import io.appium.java_client.android.{AndroidElement, AndroidKeyCode}
import org.apache.log4j.Logger
import org.openqa.selenium.{By, WebDriverException}

import scala.collection.mutable.Map

class AppTraversal private[winkar](var appPath: String) {
  var logDir: String = null
  private var appPackage: String = null
  private var appiumAgent: AppiumAgent = null
  val maxDepth = 4
  val log: Logger = Logger.getLogger(Automator.getClass.getName)
  val depthMap = Map[String,Int]()

  private def createLogDir: Boolean = {
    var file: File = null
    logDir = s"log${File.separator}$appPackage${File.separator}${new Date().toString.replace(' ', '_')}"
    logDir = Paths.get("log", appPackage, s"${new Date().toString.replace(' ', '_')}").toString
    file = new File(logDir)
    file.mkdirs
  }

  val activityVisited = Map[String, Boolean]()
  val elements = Map[String, UiElement]()
  var currentDepth = 0


  def getClickableElements: List[UiElement] = {
    val activity = appiumAgent.driver.currentActivity

    //TODO 生成UiElement的逻辑中有部分代码被重复调用,可以考虑坐下修改进行简化
    val clickables = appiumAgent.findElements(By.xpath("//*[@clickable='true']"))
      .map( elm => (elm , UiElement.toUrl(activity, elm)))
      .map(
        elm => elements.getOrElseUpdate(elm._2, new UiElement(elm._1, activity))
      )
    clickables.filter(_.shouldClick)
  }

  def traversal(currentActivity: String) {
    val depth = depthMap.getOrElseUpdate(currentActivity, currentDepth)
    log.info("Current at " + currentActivity)
    log.info("Current traversal depth is " + depth)
    appiumAgent.takeScreenShot(logDir)


    depth match {
      case x: Int if x >= maxDepth => log.info("Reach maximum depth; Back")
      case _ => {
        var clickableElements = getClickableElements
        log.info(s"${clickableElements.size} clickable elements found on Acitivity")

        clickableElements.foreach( element => {
          try {
            log.info("Clicked " + element.toString)
            element.click

            val appActivity = appiumAgent.currentActivity

            // TODO: 对Activity进行过滤
            if (appActivity != currentActivity) {
              log.info("Jumped to activity " + appiumAgent.currentActivity)

              appiumAgent.currentPackage match {
                case pkg:String if pkg!=appPackage => {
                  log.info("Jumped out of App")
                  log.info(s"Current at app ${pkg}")
                  log.info("Back to previous")
                  while (appiumAgent.currentPackage != appPackage) back
                }
                case _ => {
                  currentDepth += 1
                  traversal(appActivity)
                  currentDepth -= 1
                }
              }
            }
          } catch {
            // UI被改变后可能出现原来的元素无法点击的情况. 跳过并加载新的元素
            case e :org.openqa.selenium.NoSuchElementException => {
              clickableElements = getClickableElements
              log.info("Reload clickable elements")
              log.info(s"${clickableElements.size} elements found")
            }
          }
        })
      }
    }

    depth match {
      case 0 => {
        appiumAgent.closeApp
        log.info("Close App")
      }
      // TODO 需要处理一次back无法跳转activity, 说明该activity无法back
      case _ => back
    }
  }

//  def back = appiumAgent.pressKeyCode(AndroidKeyCode.BACK)
  def back = appiumAgent.driver.navigate().back()

  @throws[IOException]
  def start {
    appiumAgent = new AppiumAgent(appPath)
    try {
      appPackage = AndroidUtils.getPackageName(appPath)
      log.info("Get package Name: " + appPackage)

      if (!createLogDir) {
        throw new IOException("Directory not created")
      }
      log.info("Traversal started")
      traversal(appiumAgent.currentActivity)
    }
    catch {
      case e: WebDriverException => e.printStackTrace
      case e: Exception => e.printStackTrace
    } finally {
      appiumAgent.takeScreenShot(logDir)
      appiumAgent.removeApp(appPackage)
      appiumAgent.quit
    }
  }
}
