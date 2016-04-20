package com.winkar.Graph

/**
  * Created by winkar on 16-4-20.
  */
class ActionEdge(graph: UiGraph, dstView: ViewNode, actionType: ActionType.Value, element: UiElement = null) {
  val action = actionType
  val parent = graph
  def Element = element

  def this(graph: UiGraph, uiElement: UiElement) = this(graph, graph.getNode(uiElement.destView), ActionType.Click, uiElement)
  val destView = dstView
}
