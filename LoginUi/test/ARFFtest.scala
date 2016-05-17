import org.apache.log4j.Logger
import org.apache.log4j.xml.DOMConfigurator
import org.junit.{Before, Test}

/**
  * Created by winkar on 16-5-5.
  */
class ARFFtest {
  var log: Logger = null

  @Before
  def init() = {
    DOMConfigurator.configureAndWatch("config/log4j.xml")
    log = Logger.getLogger("ARFF")
  }

  @Test
  def testArffGenerate(): Unit = {
    log.info("Test Start")
    new ARFFUtil().generateARFF()
    log.info("Test End")
  }
}
