package com.winkar

/**
  * Created by winkar on 16-4-11.
  */

import java.io.PrintWriter

import scala.collection.{Set, mutable}


class ViewNode(graph: UiGraph, view: String) {
  val parent = graph
  def View = view

  val aliasView = mutable.Set[String]()

  val edges = mutable.HashSet[ActionEdge]()

  var visited = false

  def visitComplete: Boolean = elementsVisited.values.forall(a=>a)

  var depth = -1

  def elements: List[UiElement] = elementsVisited.keySet.toList

  val elementsVisited = mutable.HashMap[UiElement, Boolean]()

  def addElement(uiElement: UiElement) = {
    elementsVisited.update(uiElement, false)
    uiElement.parentView = this
  }

  def addAlias(view: String) = {
    aliasView.add(view)
    parent.addNode(view, this)
  }

  def hasAlias(view: String) = aliasView.contains(view)

  def removeElement(uiElement: UiElement) = elementsVisited.remove(uiElement)

  def addAllElement(elements: Seq[UiElement]) = elements.foreach(addElement)

  def addEdge(element: UiElement) = edges.add(new ActionEdge(parent, element))

  def addEdge(edge: ActionEdge) = edges.add(edge)

  def toXml = {
    <Node view={view} elementCount={elementsVisited.size.toString} depth={depth.toString}>
      {edges.map(edge => <Edge>
                            <To>{edge.destView.View}</To>
                            <Click>{edge.Element.toString}</Click>
                        </Edge>)
                      }
    </Node>
  }
}

object ActionType extends Enumeration {
  val Click, Auto = Value
}

class ActionEdge(graph: UiGraph, dstView: ViewNode, actionType: ActionType.Value, element: UiElement = null) {
  val action = actionType
  val parent = graph
  def Element = element

  def this(graph: UiGraph, uiElement: UiElement) = this(graph, graph.getNode(uiElement.destView), ActionType.Click, uiElement)
  val destView = dstView
}

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
