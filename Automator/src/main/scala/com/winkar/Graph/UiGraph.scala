package com.winkar.Graph

import java.io.PrintWriter

import scala.collection.mutable

/**
  * Created by winkar on 16-4-20.
  */
class UiGraph {
  val nodes = mutable.Map[String, ViewNode]()
  val visitCompleteMap = mutable.HashSet[ViewNode]()


  def getNode(view: String) = nodes.getOrElseUpdate(view, new ViewNode(this, view))

  def addNode(view: String) = nodes.update(view, new ViewNode(this, view))
  def addNode(view: String, node: ViewNode) = nodes.update(view, node)

  def toXml = {
    <UI>
      {nodes.values.map(_.toXml)}
    </UI>
  }

  def saveXml(path: String) = {
//    xml.XML.save(path, toXml, "UTF-8")
    val pp = new xml.PrettyPrinter(80, 4)
    val siteFile = new PrintWriter(path)
    siteFile.append(pp.format(toXml))
    siteFile.flush()
  }
}
