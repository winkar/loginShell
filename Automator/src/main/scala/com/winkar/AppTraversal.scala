package com.winkar

import java.io.{File, IOException, PrintWriter, StringWriter}
import java.nio.file.Paths
import java.util.Date

import org.apache.log4j.Logger
import org.openqa.selenium.By

import scala.collection.mutable
import scala.concurrent._



class AppTraversal private[winkar](var appPath: String) {
  var logDir: String = ""
  private var appPackage: String = ""
  private var appiumAgent: AppiumAgent = null
  val maxDepth = 4
  val log: Logger = Logger.getLogger(Automator.getClass.getName)
  val depthMap = mutable.Map[String,Int]()

  var lastClickedElement : UiElement = null

  val jumpStack = mutable.Stack[String]()

  def lastView: String = {
    if (jumpStack.isEmpty) {
      ""
    } else {
      jumpStack.top
    }
  }


  class ShouldRestartAppException extends RuntimeException
  class ViewChangedException extends RuntimeException
  class LoginUiFoundException(loginUI: String) extends RuntimeException {
    val loginUi = loginUI
  }

  private def createLogDir: Boolean = {
    var file: File = null
    logDir = s"log${File.separator}$appPackage${File.separator}${new Date().toString.replace(' ', '_')}"
    logDir = Paths.get("log", appPackage, s"${new Date().toString.replace(' ', '_')}").toString
    file = new File(logDir)
    file.mkdirs
  }

  val elements = mutable.Map[String, UiElement]()
  var currentDepth = 0


  def getClickableElements(retryTime :Int = 1): List[UiElement] = {
    val view = getCurrentView

    //TODO 生成UiElement的逻辑中有部分代码被重复调用,可以考虑坐下修改进行简化
    val clickableElements = appiumAgent.findElements(By.xpath("//*[@clickable='true']"))
      .map( elm => (elm , UiElement.toUrl(view, elm)))
      .map(
        elm => elements.getOrElseUpdate(elm._2, new UiElement(elm._1, view))
      )


//    val p = new scala.xml.PrettyPrinter(80, 4)
//    log.info(p.format(XML.loadString(appiumAgent.driver.getPageSource))  )

    clickableElements.filter(_.shouldClick) match {
      case cl: List[UiElement] if retryTime==0 || cl.nonEmpty => cl
      case cl: List[UiElement] if cl.isEmpty  =>
        log.info("Cannot find any element; Sleep and try again")
        Thread.sleep(3000)
        getClickableElements(0)
    }
  }

  def checkPermissions(): Unit ={
    log.info("Checking All Permissions")
//    appiumAgent.driver.findElementByClassName("android.widget.CheckBox").click
    // TODO 是否可能默认没有check上?
    log.info("Confirm")
    appiumAgent.driver.findElementByClassName("android.widget.Button").click()
  }

  def getCurrentView: String = s"${appiumAgent.driver.currentActivity}_${MessageDigest.Md5(appiumAgent.driver.getPageSource)}"

  def checkCurrentPackage() = if (appiumAgent.currentPackage != appPackage) throw new ShouldRestartAppException


  def traversal() {
    val currentView = getCurrentView

    val depth = depthMap.getOrElseUpdate(currentView, currentDepth)
    log.info("Current at " + currentView)
    log.info("Current traversal depth is " + depth)
    appiumAgent.takeScreenShot(logDir)



    depth match {
      case x: Int if x >= maxDepth => log.info("Reach maximum depth; Back")
      case _ =>
        var clickableElements = getClickableElements()
        log.info(s"${clickableElements.size} clickable elements found on view")
        if (LoginUI.isLoginUI(lastClickedElement, clickableElements, currentView)) {
          throw new LoginUiFoundException(currentView)
        }

        try {
          clickableElements.foreach(element => {
            try {
              log.info("Click " + element.toString)
              element.click
              lastClickedElement = element

              val viewAfterClick = getCurrentView
              element.destView = viewAfterClick

              if (element.destView==lastView) {
                element.isBack = true
              }

              if (viewAfterClick != currentView) {
                log.info("Jumped to view " + viewAfterClick)

                appiumAgent.currentPackage match {
                  case pkg: String if pkg != appPackage =>
                    log.info("Jumped out of App")
                    log.info(s"Current at app $pkg")

                    if (pkg == "com.sec.android.app.capabilitymanager") checkPermissions()

                    log.info("Try back to app")

                    back()

                    // 如果无法回到原App, 重新启动App
                    checkCurrentPackage()
                  case _ =>
                    currentDepth += 1
                    jumpStack.push(currentView)
                    traversal()
                    jumpStack.pop()
                    currentDepth -= 1
                }
              }
            } catch {
              // UI被改变后可能出现原来的元素无法点击的情况. 跳过并加载新的元素
              case e: org.openqa.selenium.NoSuchElementException =>
                checkCurrentPackage()
                if (getCurrentView!=currentView) throw new ViewChangedException
                log.info("Cannot locate element")
                log.info("Reload clickable elements")
                clickableElements = getClickableElements()
                log.info(s"${clickableElements.size} elements found")
            }
          })
        } catch {
          case ex: ShouldRestartAppException =>
            restartApp()
            traversal()
          case ex: ViewChangedException =>
            log.info("View changed unexpected")
            log.info(s"Current view is ${currentView}")
            // Do nothing but jump out of inner foreach loop
        }
    }

    back()
  }

  def back() = {
    log.info("Back")
    appiumAgent.driver.navigate().back()
  }

  def restartApp() = {
    log.info("Restart App")
    appiumAgent.driver.launchApp()
  }


  val traversalTimeout = 10

  def start() {
    appiumAgent = new AppiumAgent(appPath)
    try {
      log.info(s"Start testing apk: ${appPath}")
      appPackage = AndroidUtils.getPackageName(appPath)
      log.info("Get package Name: " + appPackage)

      if (!createLogDir) {
        throw new IOException("Directory not created")
      }
      log.info("Traversal started")

      import java.util.concurrent.{Callable, FutureTask, TimeUnit}
      val traversalTask = new FutureTask(new Callable[Unit]  {
        def call(): Unit = {
          traversal()
        }
      })

      try {
        new Thread(traversalTask).start()
        traversalTask.get(traversalTimeout, TimeUnit.MINUTES)
      } catch  {
        case e: java.util.concurrent.ExecutionException =>
          throw e.getCause()
      }
    }
    catch {
      case e: LoginUiFoundException =>
        log.warn(s"Login Ui Found: ${e.loginUi} in package ${this.appPackage} at ${appPath}")
      case e: TimeoutException =>
        log.warn("Timeout!")
      case e: Exception =>
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        log.warn(sw.toString)
    } finally {
      log.info("Take screenShot on quit")
      appiumAgent.takeScreenShot(logDir)
      log.info("Remove app from device")
      appiumAgent.removeApp(appPackage)
      log.info("Quit")
      appiumAgent.quit()
    }
  }
}
