package com.winkar

/**
  * Created by winkar on 16-4-11.
  */

import java.io.PrintWriter

import scala.collection.mutable

class ViewNode(graph: UiGraph, view: String) {
  val parent = graph
  def View = view
  val edges = mutable.ListBuffer[ActionEdge]()

  def addEdge(element: UiElement) = edges.append(new ActionEdge(parent, element))

  def toXml = {
    <Node view={view}>
      {edges.map(edge => <Edge>
                            <To>{edge.destView.View}</To>
                            <Click>{edge.Element.toString}</Click>
                        </Edge>)
                      }
    </Node>
  }
}

class ActionEdge(graph: UiGraph, element: UiElement) {
  val parent = graph
  def Element = element
  val destView = parent.getNode(element.destView)
}

class UiGraph {
  val nodes = mutable.Map[String, ViewNode]()

  def getNode(view: String) = nodes.getOrElseUpdate(view, new ViewNode(this, view))

  def addNode(view: String) = nodes.update(view, new ViewNode(this, view))

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
