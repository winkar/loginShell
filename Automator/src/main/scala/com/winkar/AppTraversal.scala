package com.winkar

import akka.actor.Actor
import com.winkar.Appium.AppiumAgent
import com.winkar.Graph.{LoginUI, UiElement, UiGraph, ViewNode}
import com.winkar.Utils.{AndroidUtils, HierarchyUtil, LogUtils, Timer}
import org.apache.log4j.Logger
import org.joda.time.Period
import org.openqa.selenium.By

import scala.collection.mutable
import scala.concurrent._


case class StartTravel(apkFilePath: Option[String])
case class NextApk()
case class Active()
case class Done()



object TravelResult extends Enumeration {
  val Complete, LoginUiFound, Fail = Value
}

case class TravelResult(cost: Period, st: TravelResult.Value, pkgName: String, apkFileName: String)

class AppTraversal(apkFullPath: String, pkgName: String)  {
  var screenShotLogDir: String = ""


  val appPath = apkFullPath
  val appPackage = pkgName
  private var appiumAgent: AppiumAgent = null

  val maxDepth = 100
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

  val uiGraph = new UiGraph(appPackage)

  class ShouldRestartAppException extends RuntimeException
  class UnexpectedViewException extends RuntimeException
  class LoginUiFoundException(loginUI: String) extends RuntimeException {
    val loginUi = loginUI
  }

  val elements = mutable.Map[String, UiElement]()


  @scala.annotation.tailrec
  final def getClickableElements(view: String, retryTime :Int = 1): List[UiElement] = {
    appiumAgent.findElements(By.xpath("//*[@clickable='true']"))
        .map(new UiElement(_, view)) match {
      case cl: List[UiElement] if retryTime==0 || cl.nonEmpty => cl
      case cl: List[UiElement] if cl.isEmpty  =>
        log.info("Cannot find any element; Sleep and try again")
        Thread.sleep(3000)
        getClickableElements(view, 0)
    }
  }

  def checkPermissions(): Unit ={
    log.info("Checking All Permissions")
    log.info("Confirm")
    appiumAgent.driver.findElementByClassName("android.widget.Button").click()
  }

  def getCurrentView: String = s"${appiumAgent.driver.currentActivity}_${HierarchyUtil.uiStructureHashDigest(appiumAgent.driver.getPageSource)}"

  def checkCurrentPackage() = if (appiumAgent.currentPackage != appPackage) throw new ShouldRestartAppException

  def checkAutoChange(currentView: String, currentNode: ViewNode) = {
    val changed = getCurrentView
    if (changed != currentView && !currentNode.hasAlias(changed)) {
      val changed = getCurrentView
      log.info(s"Automated changed to view: $changed; Add alias")
      currentNode.addAlias(changed)
    }
  }

  def traversal(expectView: String = null) {
    val currentView = getCurrentView
    val currentNode = uiGraph.getNode(currentView)
//    val depth = depthMap.getOrElseUpdate(currentView, currentDepth)

    if (expectView!=null  && currentView != expectView) {
      log.info(s"Automated changed to view: $currentView; Add alias")
      currentNode.addAlias(expectView)
    }

    val nodeVisited = currentNode.visited
    currentNode.visited = true


    val depth = currentNode.depth match {
      case -1 =>
        currentNode.depth = if (lastView != "") uiGraph.getNode(lastView).depth + 1 else 0
        currentNode.depth
      case d: Int  => d
    }

    log.info("Current at " + currentView)
    log.info("Current traversal depth is " + depth)
    appiumAgent.takeScreenShot(screenShotLogDir, currentNode.name)

//    log.info(xml.XML.loadString(appiumAgent.driver.getPageSource))

    depth match {
      case x: Int if x >= maxDepth =>
        log.info("Reach maximum depth; Back")
        // 直接回到上一个View
        back()
      case _ =>
        var clickableElements = nodeVisited match  {
          case true => currentNode.elements
          case false =>
            val elems: List[UiElement] = getClickableElements(currentView)
            checkAutoChange(currentView, currentNode)
            currentNode.addAllElement(elems)
            elems
        }

        clickableElements = clickableElements.filter(!_.visited)  ++ clickableElements.filter(_.visited)

        log.info(s"${clickableElements.size} clickable elements found on view")


        if (LoginUI.isLoginUI(lastClickedElement, clickableElements, currentView)) {
          throw new LoginUiFoundException(currentView)
        }

        try {
          clickableElements.foreach(element => {
            currentNode.elementsVisited(element) = true


            if (element.shouldClick) {
              try {

                if (!element.visited || (element.visited && !element.destViewVisitComplete
                  && !jumpStack.contains(element.destView))) {
                  log.info("Click " + element.toString)
                  element.click()
                  lastClickedElement = element

                  val viewAfterClick = getCurrentView
                  val nodeAfterClick = uiGraph.getNode(viewAfterClick)
                  element.destView = viewAfterClick

                  if (nodeAfterClick.hasAlias(lastView)) {
                    element.isBack = true
                  }

                  // 判断是否变换了view应当根据node而非view
                  if (! currentNode.hasAlias(viewAfterClick)) {
                    log.info("Jumped to view " + viewAfterClick)

                    appiumAgent.currentPackage match {
                      case pkg: String if pkg != appPackage =>
                        log.info("Jumped out of App")
                        log.info(s"Current at app $pkg")

                        element.willJumpOutOfApp = true
                        if (pkg == "com.sec.android.app.capabilitymanager") checkPermissions()

                        log.info("Try back to app")

                        back()

                        // 如果无法回到原App, 重新启动App
                        checkCurrentPackage()
                      //                      checkCurrentView(expectedView = currentView)
                      case _ =>
                        // 仅当目标view在app内且非原来View时才将其加入边集中
                        currentNode.addEdge(element)

                        jumpStack.push(currentView)
                        traversal(viewAfterClick)
                        while (jumpStack.top != currentView) jumpStack.pop()
                    }
                  }
                }
              }
              catch {
                // UI被改变后可能出现原来的元素无法点击的情况. 跳过并加载新的元素
                case e: org.openqa.selenium.NoSuchElementException =>

                 // 这个exception会在某个element被点击但不存在的时候出现
                 // 该被点击的Element在上面已经被标记为visited, 但并未实际访问过
                 // 因此需要将状态复原
                  currentNode.elementsVisited(element) = false

                  checkCurrentPackage()

                  // 如果是在App内的某个View, 但不是当前应该在的View, 则跳过当前所有element
                  // 可能由于上一个element访问后未back到本View导致
                  if (!currentNode.hasAlias(getCurrentView)) throw new UnexpectedViewException

//                  checkAutoChange(currentView, currentNode)


                  log.info("Cannot locate element")
                  log.info("Reload clickable elements")
                  clickableElements = getClickableElements(currentView  )
                  currentNode.addAllElement(clickableElements)
                  log.info(s"${clickableElements.size} elements found")
              }
            }
          })


          //某个element点击后没有回到当前View, 同时又没有点击下一个element, 这时就需要专门check一遍是否还在正确的view上, 否则不用back
          if (currentNode.hasAlias(getCurrentView)) back()

        }
        catch {
          case ex: ShouldRestartAppException =>
            restartApp()
            traversal()
          case ex: UnexpectedViewException =>
            log.info("View changed unexpected")
            log.info(s"Current view is $getCurrentView")
          // Do nothing but jump out of inner foreach loop
        }
    }
  }

  def back() = {
    log.info("Back")
    appiumAgent.driver.navigate().back()
  }

  def restartApp() = {
    log.info("Restart App")
    appiumAgent.driver.launchApp()
  }


  val traversalTimeout = 15


  def start(): TravelResult.Value = {
    try {
    // 如果初始化不了drvier就给我一直重启吧
      while (appiumAgent == null) {
        try {
          appiumAgent = new AppiumAgent(appPath)
        } catch  {
          case _: org.openqa.selenium.SessionNotCreatedException | _:org.openqa.selenium.remote.UnreachableBrowserException =>
            GlobalConfig.server.restart()
        }
      }

      log.info(s"Start testing apk: $appPath")
      log.info(s"Package name: $appPackage")

      LogUtils.initLogDirectory(appPackage)
      screenShotLogDir = LogUtils.screenshotLogDir

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
          throw e.getCause
      }
      TravelResult.Complete
    }
    catch {
      case e: LoginUiFoundException =>
        log.warn(s"Login Ui Found: ${e.loginUi} in package ${this.appPackage} at $appPath")
        TravelResult.LoginUiFound
      case e: TimeoutException =>
        log.warn("Timeout!")
        TravelResult.Fail
      case e: org.openqa.selenium.WebDriverException =>
//        LogUtils.printException(e)
        log.info("Unknown appium exception")
        TravelResult.Fail
      case e: Exception =>
        LogUtils.printException(e)
        TravelResult.Fail
    } finally {
      log.info("Take screenShot on quit")
      if (appiumAgent!=null) {
        appiumAgent.takeScreenShot(screenShotLogDir, "Quit")
        uiGraph.saveXmlAndDotFile(LogUtils.siteXmlPath)
        import scala.sys.process._
        s"dot ${LogUtils.packagelogDir}/site.dot -Tpng -o ${LogUtils.packagelogDir}/site.png".!
        log.info("Remove app from device")
        appiumAgent.removeApp(appPackage)
        log.info("Quit")
        appiumAgent.quit()
        appiumAgent = null
      }
    }
  }
}


// 之前的写法把多个apk跑在了同一个traveler当中, 已经不能用问题大形容了...各种忘记初始化, 还是分开来简单
class TravelMonitor extends Actor {

  val timer = new Timer

  override def receive: Receive = {
    case Active =>
      sender ! NextApk
    case StartTravel(o_apk: Option[String]) =>
      o_apk match {
        case Some(apkFilePath: String) =>
          timer.start
          val packageName = AndroidUtils.getPackageName(apkFilePath)
          var travelResult: TravelResult.Value = null
          // 捕捉所有未handle的异常, 一旦产生直接判定为fail(所有appium client的代码应该都在里面了)
          try {
            travelResult = new AppTraversal(apkFilePath, packageName).start()
          } catch {
            case _: Exception => travelResult =TravelResult.Fail
          }
          val period = timer.stop
          sender ! TravelResult(period, travelResult, apkFilePath, packageName)
          sender ! NextApk
        case None =>
          sender ! Done
      }
  }
}
