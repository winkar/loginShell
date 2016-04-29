package com.winkar.Graph

import com.winkar.Automator
import com.winkar.Utils.LogUtils
import org.apache.log4j.Logger

import scala.collection.mutable

/**
  * Created by winkar on 16-4-20.
  */
class ViewNode(graph: UiGraph, view: String) {
  val log: Logger = Logger.getLogger(Automator.getClass.getName)

  val parent = graph
  def View = view
  var id = parent.getNewId
  val name = s"node$id"
  var shouldWait = false

  val aliasView = mutable.Set[String]()
  aliasView.add(view)

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

  def mergeNode(node: ViewNode) = {
    // node 有可能为getNode产生的新Node, 此时node的depth为-1

    log.info(s"Merge node${node.id} and node$id")

    if (node.depth != -1 && node.depth < this.depth) {
      this.depth = node.depth
    }

    if ( node.id < this.id) {
      this.id = node.id
    }

    node.aliasView.foreach(v => {
      this.aliasView.add(v)
      parent.update(v, this)
    })

    //不需要专门删除原来的node, 因为已经没有reference指向它(理论上应该是这样的吧?)
  }

  def addAlias(view: String) = {
    mergeNode(parent.getNode(view))
  }

  def hasAlias(view: String) = aliasView.contains(view)

  def removeElement(uiElement: UiElement) = elementsVisited.remove(uiElement)

  def addAllElement(elements: Seq[UiElement]) = elements.foreach(addElement)

  def addEdge(element: UiElement) = edges.add(new ActionEdge(parent, element))

  def addEdge(edge: ActionEdge) = edges.add(edge)


  def toXml = {
    <Node id={name} elementCount={elementsVisited.size.toString} depth={depth.toString}>
      <Views>
        {this.aliasView.map(view => <View>{view}</View>)}
      </Views>
      <Edges>
      {edges.filter(edge => !edge.Element.willJumpOutOfApp).map(edge => <Edge>
                            <To>{parent.getNode(edge.destView.View).name}</To>
                            <Click>{edge.Element.toString}</Click>
                        </Edge>)
                      }
      </Edges>
    </Node>
  }
}
