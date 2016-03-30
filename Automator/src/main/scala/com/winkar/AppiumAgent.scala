package com.winkar

import java.net.URL
import java.nio.file.Paths

import io.appium.java_client.android.{AndroidDriver, AndroidElement}
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger
import org.openqa.selenium.remote.{CapabilityType, DesiredCapabilities}
import org.openqa.selenium.{By, OutputType}

import scala.collection.JavaConverters._


class AppiumAgent(val appPath: String) {
  val capabilities: DesiredCapabilities = new DesiredCapabilities
  capabilities.setCapability("deviceName", "Galaxy Note4")
  capabilities.setCapability("platformVersion", "4.4")
  capabilities.setCapability("platformName", "Android")
  capabilities.setCapability("app", appPath)
  val driver = new AndroidDriver[AndroidElement](new URL("http://localhost:4723/wd/hub"), capabilities)


  val log: Logger = Logger.getLogger(Automator.getClass.getName)
  var screenShotCounter: Int = 0



  def takeScreenShot(logDir: String) {
    val screenShotFile: Array[Byte] = driver.getScreenshotAs(OutputType.BYTES)

    FileUtils.writeByteArrayToFile(Paths.get(logDir, s"$screenShotCounter.png").toFile, screenShotFile)
    screenShotCounter += 1
  }

  def currentActivity: String = driver.currentActivity

  def currentPackage: String = AndroidUtils.getCurrentPackage

  def pressKeyCode(key: Int) = driver.pressKeyCode(key)


  def findElements(by: By): List[AndroidElement] = driver.findElements(by).asScala.toList

  def quit = driver.quit

  def closeApp = driver.closeApp

  def installApp(apkPath: String) = driver.installApp(apkPath)

  def removeApp(bundleId: String) = driver.removeApp(bundleId)

  def startActivity(appPackage: String, activity: String) = driver.startActivity(appPackage, activity)
}
