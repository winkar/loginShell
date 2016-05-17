
import scala.xml._


object Feature {
  def getNodeCountAndMaxDepth(node: Node): (Int, Int) = {
    var nodeCount = 0
    var maxDepth = 0
    node.child.foreach(
      n => {
        val result = getNodeCountAndMaxDepth(n)
        nodeCount += result._1
        if (result._2 +1 > maxDepth) maxDepth = result._2 + 1
      }
    )

    (nodeCount+1, maxDepth)
  }


  def getEditTextCount(node: Node) = ( node \\ "android.widget.EditText").size

  def getEditTextText(node: Node) = (node \\ "android.widget.EditText").map(_ \ "@resource-id").mkString("_")


  def getClickableCount(node: Node) =  (node \\ "_").count(p => (p \ "@clickable" text) == "true")
  def getClickableText(node: Node) = (node \\ "_").filter(p => (p \ "@clickable" text) == "true").map(_ \ "@content-desc").mkString("_")

  def getActivityName(viewName: String) = viewName.split("_").dropRight(1).mkString("_")



  def apply(viewName: String, node: Node, isLogin: Boolean): Feature = {
    new Feature(
      viewName,
      node,
      isLogin
    )
  }

  def apply(viewName: String, filename: String, isLogin: Boolean): Feature = apply(viewName, XML.loadFile(filename), isLogin)
}

/**
  * Created by winkar on 16-5-5.
  */
class Feature(viewName: String, node: Node, isLogin: Boolean) {
  import Feature._


  // 一个login界面往往有1~3个输入框, 若干按钮
  val EditTextCount = getEditTextCount(node)
  val ClickableElementCount = getClickableCount(node)


  val dfsResult = getNodeCountAndMaxDepth(node)

  // 一个Login界面的复杂度应该低于普通界面
  val HierarchyNodeCount = dfsResult._1
  val MaxHierarchyDepth = dfsResult._2

  // 可能具有特定的activity name 模式
//  val ActivityName = activityName

  val IsLogin = isLogin

  override def toString = Seq(
    EditTextCount,
    ClickableElementCount,
    HierarchyNodeCount,
//    getActivityName(viewName),
    MaxHierarchyDepth,
    IsLogin).
    mkString(",") + s"% $viewName"
//  override def toString() = s"$EditTextCount,$ClickableElementCount,$MaxHierarchyDepth,$HierarchyNodeCount,$IsLogin"
}
