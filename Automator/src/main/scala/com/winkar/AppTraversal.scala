package com.winkar

import java.io._

import akka.actor.Actor
import com.winkar.Appium.AppiumAgent
import com.winkar.Graph.{LoginUI, UiElement, UiGraph, ViewNode}
import com.winkar.Utils.{AndroidUtils, HierarchyUtil, LogUtils, Timer}
import org.apache.log4j._
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


  def getActivityName(view: String) = view.split("_").dropRight(1).mkString("")

  val appPath = apkFullPath
  val appPackage = pkgName
  private var appiumAgent: AppiumAgent = null

  val maxDepth = 100
  val log: Logger = Logger.getLogger(Automator.getClass.getName)
  val depthMap = mutable.Map[String,Int]()
  val pageSourceMap = mutable.Set[String]()

  var lastClickedElement : UiElement = null

  val jumpStack = mutable.Stack[String]()

  def lastView: String = {
    if (jumpStack.isEmpty) {
      ""
    } else {
      jumpStack.top
    }
  }

  val uiGraph = new UiGraph(appPackage, appPath)

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
        // 如果上一次find elements时是需要wait的, 那么这次应当也要, 否则会导致无法点击
        uiGraph.getNode(view).shouldWait = true
        Thread.sleep(3000)
        getClickableElements(view, 0)
    }
  }

  def checkPermissions(): Unit ={
    log.info("Checking All Permissions")
    log.info("Confirm")
    appiumAgent.driver.findElementByClassName("android.widget.Button").click()
  }

  def getCurrentView: String = {
    try {
      s"${appiumAgent.driver.currentActivity}_${HierarchyUtil.uiStructureHashDigest(appiumAgent.driver.getPageSource)}"
    } catch {
      case e: NullPointerException => throw new ShouldRestartAppException
    }
  }
  def checkCurrentPackage() =  {
    while (AndroidUtils.getCurrentPackage() == "com.sec.android.app.capabilitymanager") checkPermissions()
    if (appiumAgent.currentPackage != appPackage) throw new ShouldRestartAppException
  }




  def checkAutoChange(currentView: String, currentNode: ViewNode) = {
    val changed = getCurrentView
    if (changed != currentView && !currentNode.hasAlias(changed)) {
      val changed = getCurrentView
      log.info(s"Automated changed to view: $changed")

      // 仅当两边Activity相同时才合并
      if (getActivityName(changed) == getActivityName(currentView)) {
        currentNode.addAlias(changed)
      } else {
        setCurrentViewAndNode(currentNode.depth)
      }
    }
  }

  var loginUiFound = false
  var currentView: String = null
  var currentNode: ViewNode = null


  def setCurrentViewAndNode(originDepth: Int) = {
    currentView = getCurrentView
    currentNode = uiGraph.getNode(currentView)
    currentNode.depth match {
      case -1 =>
        currentNode.depth = originDepth + 1
      case _: Int  =>
    }
    appiumAgent.takeScreenShot(screenShotLogDir, currentNode.name)
    log.info("Current at " + currentView)
    log.info(s"Current at node${currentNode.id}")
    log.info("Current traversal depth is " + currentNode.depth)
  }

  def traversal(expectView: String = null) {
    currentView = getCurrentView
    currentNode = uiGraph.getNode(currentView)

    //说出来你可能不信, 真的有一开始就不在里面的App....
    if (AndroidUtils.getCurrentPackage()!= appPackage) {
      return
    }

//    val depth = depthMap.getOrElseUpdate(currentView, currentDepth)

    if (expectView!=null  && currentView != expectView) {
      log.info(s"Automated changed to view: $currentView; Add alias")
      if (getActivityName(expectView) == getActivityName(currentView)) {
        currentNode.addAlias(expectView)
      }
    }

    if (!pageSourceMap.contains(currentView)) {
      LogUtils.logLayout(currentView, appiumAgent.driver.getPageSource)
      pageSourceMap.add(currentView)
      checkAutoChange(currentView, currentNode)
    }


    val nodeVisited = currentNode.visited
    currentNode.visited = true


    currentNode.depth match {
      case -1 =>
        currentNode.depth = if (lastView != "") uiGraph.getNode(lastView).depth + 1 else 0
      case _: Int  =>
    }

    log.info("Current at " + currentView)
    log.info(s"Current at node${currentNode.id}")
    log.info("Current traversal depth is " + currentNode.depth)

//    log.info(xml.XML.loadString(appiumAgent.driver.getPageSource))

    currentNode.depth match {
      case x: Int if x >= maxDepth =>
        log.info("Reach maximum depth; Back")
        // 直接回到上一个View
        back()
      case _ =>
        var clickableElements = nodeVisited match  {
          case true =>
            if (currentNode.shouldWait) Thread.sleep(3000)
            currentNode.elements
          case false =>
            appiumAgent.takeScreenShot(screenShotLogDir, currentNode.name)
            val elems: List[UiElement] = getClickableElements(currentView)
            checkAutoChange(currentView, currentNode)
            currentNode.addAllElement(elems)
            elems
        }

        clickableElements = clickableElements.filter(!_.visited)  ++ clickableElements.filter(_.visited)

        log.info(s"${clickableElements.size} clickable elements found on view")


        if (!loginUiFound && LoginUI.isLoginUI(lastClickedElement, clickableElements, currentView)) {
//          throw new LoginUiFoundException(currentView)
          loginUiFound = true
          appiumAgent.takeScreenShot(screenShotLogDir, "Login")
          log.warn(s"Login Ui Found: $currentView in package ${this.appPackage} at $appPath")
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
                  element.destView = viewAfterClick



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
                        // 仅当目标view在app内且非原来View时才将其加入图中

                      val nodeAfterClick = uiGraph.getNode(viewAfterClick)
                        if (nodeAfterClick.hasAlias(lastView)) {
                          element.isBack = true
                        }

                        currentNode.addEdge(element)

                        jumpStack.push(currentView)
                        traversal(viewAfterClick)
                        while (jumpStack.nonEmpty && jumpStack.top != currentView) jumpStack.pop()
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
            log.warn("ShouldRestartApp")
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


  def initAppiumAgent(): Unit = {
    try {
      appiumAgent = new AppiumAgent(appPath)
    } catch {
      case _: org.openqa.selenium.SessionNotCreatedException | _:org.openqa.selenium.remote.UnreachableBrowserException =>
        GlobalConfig.server.restart()
        Thread.sleep(5000)
        initAppiumAgent()
    }

  }


  def start(): TravelResult.Value = {
    try {
      log.info(s"Start testing apk: $appPath")
      log.info(s"Package name: $appPackage")

    // 如果初始化不了drvier就给我一直重启吧
      initAppiumAgent()


      screenShotLogDir = LogUtils.caseLogDir

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
      if (loginUiFound) TravelResult.LoginUiFound else TravelResult.Complete
    }
    catch {
      case e: LoginUiFoundException =>
        log.warn(s"Login Ui Found: ${e.loginUi} in package ${this.appPackage} at $appPath")
        TravelResult.LoginUiFound
      case e: TimeoutException =>
        log.warn("Timeout!")
        TravelResult.Fail
      case e: org.openqa.selenium.WebDriverException =>
        LogUtils.printException(e)
        log.error("Unknown appium exception")
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

  val log = LogUtils.getLogger


  val timer = new Timer

  override def receive: Receive = {
    case Active =>
      sender ! NextApk
    case StartTravel(o_apk: Option[String]) =>
      o_apk match {
        case Some(apkFilePath: String) =>
          val packageName = AndroidUtils.getPackageName(apkFilePath)
          LogUtils.initLogDirectory(packageName)

          // 初始化WriterAppender, 将这段log发送往指定文件
          val writer: Writer = new PrintWriter(LogUtils.caseLogPath)
          val appender = new WriterAppender(new PatternLayout("%-d{yyyy-MM-dd HH:mm:ss} [%p] %m%n"), writer)
          appender.setName(packageName)
          appender.setImmediateFlush(true)
          log.addAppender(appender)


          timer.start

          var travelResult: TravelResult.Value = null
          // 捕捉所有未handle的异常, 一旦产生直接判定为fail(所有appium client的代码应该都在里面了)
          try {
            travelResult = new AppTraversal(apkFilePath, packageName).start()
          } catch {
            case _: Exception => travelResult =TravelResult.Fail
          }
          val period = timer.stop


          //重新去除appender
          log.removeAppender(appender)
          writer.flush()
          writer.close()
          sender ! TravelResult(period, travelResult, apkFilePath, packageName)
          sender ! NextApk
        case None =>
          sender ! Done
      }
  }
}
