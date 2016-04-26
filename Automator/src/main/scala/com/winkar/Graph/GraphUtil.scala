package com.winkar.Graph


import java.nio.file.Paths

import com.winkar.Utils.LogUtils

import scala.xml._
/**
  * Created by winkar on 16-4-20.
  */
object GraphUtil {

  /**
    * 将当前输出的site.xml转换为dot格式
    *
    * @param xml site.xml
    * @param graphName 图名
    * @return dotfile的内容字符串
    */
  def xmlToDot(xml: Node, graphName: String): String  = {
    val sb = new StringBuilder

    sb.append(s"digraph $graphName {\n")

    (xml \\ "Node")   .foreach(
      node => {
        val nodeName = (node \ "@id").text
        sb.append(s"$nodeName [shape=box, label=<\n")
        sb.append("""<table border="0">""")

//        val elementCountTr = s"<tr><td>elementCount</td>\n<td>${(node \ "@elementCount").text}</td></tr>\n"
//        val depthTr = s"<tr><td>depth</td>\n<td>${(node \ "@depth").text}</td></tr>\n"
//        val viewTr = s"<tr><td>Views</td>\n<td>${(node \\ "View").map(_.text).mkString("\n")}</td></tr>\n"

        // 愚蠢的Scala不同时支持字符串内插和双引号转义, Ruby大法好, 天诛Scala
        val imageTr = String.format("<tr><td width=\"250\" height=\"400\" fixedsize=\"true\"><img scale=\"true\" src=\"%s.png\"></img></td></tr>", Paths.get(LogUtils.screenshotLogDir, nodeName).toAbsolutePath.toString)

//        sb.append(elementCountTr)
//        sb.append(depthTr)
//        sb.append(viewTr)
        sb.append(imageTr)

        sb.append("</table>")
        sb.append(">]\n")

        for (edge <- node \\ "Edge") {
          sb.append(String.format("%s -> %s [label=\"%s\"]\n", nodeName, (edge \ "To").text, (edge \\ "Click").text))
        }
      }
    )

    sb.append("}\n")
    sb.toString()
  }

  def xmlToDot(xmlString: String, graphName: String): String = xmlToDot(XML.loadString(xmlString), graphName)
}
