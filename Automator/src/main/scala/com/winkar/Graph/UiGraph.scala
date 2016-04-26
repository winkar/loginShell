package com.winkar.Graph

import java.io.PrintWriter

import com.winkar.Utils.LogUtils
import scala.collection.mutable
import scala.xml.PrettyPrinter

/**
  * Created by winkar on 16-4-20.
  */
class UiGraph(packageName: String) {
  val name = packageName
  val nodes = mutable.Map[String, ViewNode]()
  val visitCompleteMap = mutable.HashSet[ViewNode]()

  var nodeCounter =  0

  /**
    * 由于一个Node可能对应多个View, 所以在Graph中对Node进行编号作为它们的名字
    * @return A new Node name
    */
  def getNewNodeName = {
    nodeCounter += 1
    s"node$nodeCounter"
  }


  def getNode(view: String) = nodes.getOrElseUpdate(view, new ViewNode(this, view))

  def addNode(view: String) = nodes.update(view, new ViewNode(this, view))
  def addNode(view: String, node: ViewNode) = nodes.update(view, node)

  def toXml = {
    <UI>
      {nodes.values.map(_.toXml)}
    </UI>
  }

  /**
    * 将graph转换成的xml文件写入指定文件
    * 将xml转换成dot写入文件
    * @param path xml的写入路径
    */
  def saveXmlAndDotFile(path: String) = {
//    xml.XML.save(path, toXml, "UTF-8")
    val pp = new PrettyPrinter(80, 4)
    val siteFile = new PrintWriter(path)
    val xml = pp.format(toXml)
    siteFile.append(xml)

    val dotFilePath = LogUtils.dotFilePath
    val dotfile = new PrintWriter(dotFilePath)
    val dot = GraphUtil.xmlToDot(xml, name)
    dotfile.append(dot)
    dotfile.flush()
    siteFile.flush()
  }
}
