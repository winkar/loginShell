package com.winkar

import java.io.{File, IOException}
import java.nio.file.Paths
import java.util.Date

import org.apache.log4j.Logger
import org.openqa.selenium.{By, WebDriverException}

import scala.collection.mutable.Map

class AppTraversal private[winkar](var appPath: String) {
  var logDir: String = ""
  private var appPackage: String = ""
  private var appiumAgent: AppiumAgent = null
  val maxDepth = 4
  val log: Logger = Logger.getLogger(Automator.getClass.getName)
  val depthMap = Map[String,Int]()

  var lastClickedElement : UiElement = null


  class ShouldRestartAppException extends RuntimeException
  class LoginUiFoundException(loginUI: String) extends RuntimeException {
    val loginActivity = loginUI
  }

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


  def getClickableElements(retryTime :Int = 1): List[UiElement] = {
    val activity = appiumAgent.driver.currentActivity

    //TODO 生成UiElement的逻辑中有部分代码被重复调用,可以考虑坐下修改进行简化
    val clickables = appiumAgent.findElements(By.xpath("//*[@clickable='true']"))
      .map( elm => (elm , UiElement.toUrl(activity, elm)))
      .map(
        elm => elements.getOrElseUpdate(elm._2, new UiElement(elm._1, activity))
      )

    clickables.filter(_.shouldClick) match {
      case cl: List[UiElement] if retryTime==0 || cl.nonEmpty => cl
      case cl: List[UiElement] if cl.isEmpty  => {
        log.info("Cannot find any element; Sleep and try again")
        Thread.sleep(3000)
        getClickableElements(0)
      }
    }
  }

  def checkPermissions: Unit ={
    log.info("Checking All Permissions")
//    appiumAgent.driver.findElementByClassName("android.widget.CheckBox").click
    // TODO 是否可能默认没有check上?
    log.info("Confirm")
    appiumAgent.driver.findElementByClassName("android.widget.Button").click
  }


  def traversal(currentActivity: String) {
    val depth = depthMap.getOrElseUpdate(currentActivity, currentDepth)
    log.info("Current at " + currentActivity)
    log.info("Current traversal depth is " + depth)
    appiumAgent.takeScreenShot(logDir)



    depth match {
      case x: Int if x >= maxDepth => log.info("Reach maximum depth; Back")
      case _ => {
        var clickableElements = getClickableElements()
        log.info(s"${clickableElements.size} clickable elements found on Acitivity")
        if (LoginUI.isLoginUI(lastClickedElement, clickableElements, currentActivity)) {
          throw new LoginUiFoundException(currentActivity)
        }

        try {
          clickableElements.foreach(element => {
            try {
              log.info("Click " + element.toString)
              element.click
              lastClickedElement = element

              val appActivity = appiumAgent.currentActivity

              // TODO: 对Activity进行过滤
              if (appActivity != currentActivity) {
                log.info("Jumped to activity " + appiumAgent.currentActivity)

                appiumAgent.currentPackage match {
                  case pkg: String if pkg != appPackage => {
                    log.info("Jumped out of App")
                    log.info(s"Current at app ${pkg}")

                    if (pkg == "com.sec.android.app.capabilitymanager") checkPermissions

                    log.info("Try back to app")

                    back

                    // 如果无法回到原App, 重新启动App
                    if (appiumAgent.currentPackage != appPackage) throw new ShouldRestartAppException

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
              case e: org.openqa.selenium.NoSuchElementException => {
                //TODO 此处应check 是否还在原来的进程 check按钮是否点击完
                log.info("Cannot locate element")
                log.info("Reload clickable elements")
                clickableElements = getClickableElements()
                log.info(s"${clickableElements.size} elements found")
              }
            }
          })
        } catch {
          case ex: ShouldRestartAppException => {
            restartApp
            traversal(appiumAgent.driver.currentActivity)
          }
        }
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

  def back = appiumAgent.driver.navigate.back

  def restartApp = appiumAgent.driver.launchApp

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
      case e: LoginUiFoundException => {
        log.warn(s"Login Ui Found: ${e.loginActivity}")
        appiumAgent.takeScreenShot(logDir)
      }
      case e: Exception => e.printStackTrace
    } finally {
      appiumAgent.takeScreenShot(logDir)
      appiumAgent.removeApp(appPackage)
      appiumAgent.quit
    }
  }
}
