import com.winkar.Appium.AppiumServer
import com.winkar.Utils.{AndroidUtils, LogUtils}
import com.winkar._
import org.apache.log4j.xml.DOMConfigurator
import org.junit.{After, Before, Test}
import org.junit.Assert.assertEquals

class RestartTest  {

  val log = LogUtils.getLogger
  val apkPath = "/home/winkar/Documents/github/LoginUI/apkCrawler/download/part/2dc50849d3ff6b3ee457e2849eb4b462c73d662f.apk"
  val pkgName = AndroidUtils.getPackageName(apkPath)
  val server = new AppiumServer

  @Before
  def initialize(): Unit = {
    DOMConfigurator.configureAndWatch("config/log4j_2.xml")

    log.info("Start Appium Server")

    GlobalConfig.server = server
    server.start()
  }


  @Test
  def testRestart(): Unit = {
    log.info("start test")
    server.stop()
    val result = new AppTraversal(apkPath, pkgName).start()
    assertEquals(TravelResult.Complete, result)
  }

  @After
  def clear(): Unit = {
    log.info("Test Done")

    if (server!=null) {
      server.stop()
    }
  }
}