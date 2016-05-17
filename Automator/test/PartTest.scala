import java.io.{File, PrintWriter, Writer}
import java.{lang => jl, util => ju}

import com.winkar.Appium.AppiumServer
import com.winkar.Utils.{AndroidUtils, LogUtils, Timer}
import com.winkar._
import org.apache.log4j.{PatternLayout, WriterAppender}
import org.apache.log4j.xml.DOMConfigurator
import org.junit.Assert._
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.junit.{After, Before, Test}

import scala.collection.mutable

object PartTest {
  var appTested = 0
  var appIgnored = 0
  val failedAppList = mutable.ListBuffer[String]()
  val loginFoundAppList = mutable.ListBuffer[String]()
  var totalApkCount = 400
  var totalTimeInSeconds = 0
  val fullTimer = new Timer
  var loginUiFoundAppTimeCost = 0

  def tested() = new File(LogUtils.siteXmlPath).exists()

  @Parameters
  def apk() = {
    val list = new ju.ArrayList[Array[jl.String]]()
    new File("/home/winkar/Documents/github/LoginUI/apkCrawler/download/part").list.
    map ("/home/winkar/Documents/github/LoginUI/apkCrawler/download/part/" + _).foreach(n => list.add(Array(n)))
    list
  }
}

@RunWith(classOf[Parameterized])
class PartTest(apkFullPath: String)  {
  val log = LogUtils.getLogger
  val server = new AppiumServer
  val apkPath = apkFullPath
  val pkgName = AndroidUtils.getPackageName(apkPath)
  var appender: WriterAppender = null
  var skip = false
  import PartTest._

  @Before
  def initialize(): Unit = {
    System.setProperty("log_home", "log")
    DOMConfigurator.configureAndWatch("config/log4j.xml")
    LogUtils.initLogDirectory(pkgName)
    skip = tested()

    if (!skip) {
      // 初始化WriterAppender, 将这段log发送往指定文件
      val writer: Writer = new PrintWriter(LogUtils.caseLogPath)
      appender = new WriterAppender(new PatternLayout("%-d{yyyy-MM-dd HH:mm:ss} [%p] %m%n"), writer)
      appender.setName(pkgName)
      appender.setImmediateFlush(true)
      log.addAppender(appender)

      log.info("Start Appium Server")
      GlobalConfig.server = server
      server.start()
    }
  }


  @Test(timeout = 1200000) // 20分钟
  def testApk(): Unit = {
    val timer = new Timer
    timer.start
    log.info("start test")
    appTested += 1

    if (!skip) {
      val result = new AppTraversal(apkPath, pkgName).start()
      val period = timer.stop

      val seconds = period.toStandardSeconds.getSeconds
      totalTimeInSeconds += seconds
      result match {
        case TravelResult.LoginUiFound =>
          loginFoundAppList.append(apkPath)
          loginUiFoundAppTimeCost += seconds
        case TravelResult.Fail => failedAppList.append(apkPath)
        case TravelResult.Complete =>
      }

      val averageTime = totalTimeInSeconds.asInstanceOf[Double]/ appTested
      val loginUiFoundAppAverageTime = loginUiFoundAppTimeCost.asInstanceOf[Double] / loginFoundAppList.size

      log.info(s"$seconds seconds used for $apkPath")
      log.info(s"${averageTime.formatted("%.2f")} seconds cost for each apk in average ")
      log.info(s"$appTested/$totalApkCount apks already tested")
      log.info(s"${loginFoundAppList.size} login Ui found")
      log.info(s"${loginUiFoundAppAverageTime.formatted("%.2f")} seconds cost for each apps in which login ui found ")
      log.info(s"${((totalApkCount - appTested) * averageTime).formatted("%.2f")} seconds remained")

      assertTrue(result == TravelResult.Complete || result == TravelResult.LoginUiFound)
    } else {
      log.info("Already tested")
      log.info("Skip")
    }
  }

  @After
  def clear(): Unit = {
    if (!skip) {
      log.info("Test Done")
      if (server!=null) {
        server.stop()
      }
      log.removeAppender(appender)
    }
  }
}
