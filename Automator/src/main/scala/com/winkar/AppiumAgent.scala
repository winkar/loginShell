package com.winkar

import java.io.File
import java.net.{MalformedURLException, URL}

import io.appium.java_client.android.{AndroidDriver, AndroidElement}
import org.apache.log4j.Logger
import org.openqa.selenium.{By, OutputType}
import org.openqa.selenium.remote.DesiredCapabilities

import scala.collection.JavaConverters._
import java.nio.file.{Path, Paths};

class AppiumAgent(val appPath: String) {
  try {
    val capabilities: DesiredCapabilities = new DesiredCapabilities
    capabilities.setCapability("deviceName", "Galaxy Note4")
    capabilities.setCapability("platformVersion", "4.4")
    capabilities.setCapability("platformName", "Android")
    capabilities.setCapability("app", appPath)
    driver = new AndroidDriver[AndroidElement](new URL("http://localhost:4723/wd/hub"), capabilities)
  }
  catch {
    case e: MalformedURLException => {
      e.printStackTrace
    }
  }

  private val log: Logger = Logger.getLogger(Automator.getClass.getName)
  private var driver: AndroidDriver[AndroidElement] = null
  private val screenShotCounter: Int = 0

  def takeScreenShot(logDir: String) {
    val screenShotFile: File = driver.getScreenshotAs(OutputType.FILE)

    if (!screenShotFile.renameTo(Paths.get(logDir, s"$screenShotCounter.png").toFile)) {
      log.info("Cannot rename file")
    }
    if (!screenShotFile.createNewFile()) {
      log.info("Cannot create file")
    }
  }

  def currentActivity: String = driver.currentActivity


  def pressKeyCode(key: Int) = driver.pressKeyCode(key)


  def findElements(by: By): List[AndroidElement] = driver.findElements(by).asScala.toList

  def quit = driver.quit

  def closeApp = driver.closeApp

  def installApp(apkPath: String) = driver.installApp(apkPath)

  def removeApp(bundleId: String) = driver.removeApp(bundleId)

  def startActivity(appPackage: String, activity: String) = driver.startActivity(appPackage, activity)

  def formatAndroidElement(element: AndroidElement): String = String.format(s"Tag: ${element.getTagName}; " +
    s"Id ${element.getId}; " +
    s"Text ${element.getText}")

}
