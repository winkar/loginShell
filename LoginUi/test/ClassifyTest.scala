import java.io.File

import org.apache.log4j.Logger
import org.apache.log4j.xml.DOMConfigurator
import org.junit.{Before, Test}
import weka.classifiers.Evaluation
import weka.classifiers.functions.LibSVM
import weka.core.Debug.Random
import weka.core.SelectedTag
import weka.core.converters.ArffLoader
/**
  * Created by winkar on 16-5-6.
  */
class ClassifyTest {
  var log: Logger = null

  @Before
  def init() = {
    DOMConfigurator.configureAndWatch("config/log4j.xml")
    log = Logger.getLogger("ARFF")
  }

  @Test
  def testClassify(): Unit = {
    val arffLoader = new ArffLoader
    arffLoader.setFile(new File("data.arff"))
    val instance = arffLoader.getDataSet

    instance.setClassIndex(instance.numAttributes()-1)

    val classifier = new LibSVM()
    classifier.setSVMType(new SelectedTag(LibSVM.SVMTYPE_C_SVC, LibSVM.TAGS_SVMTYPE))
    classifier.buildClassifier(instance)

    val evaluation = new Evaluation(instance)
    evaluation.crossValidateModel(classifier, instance, 10, new Random(1))

    log.info("")
    log.info(s"error rate: ${evaluation.errorRate()}")
  }
}
