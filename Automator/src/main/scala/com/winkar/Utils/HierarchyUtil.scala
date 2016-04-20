package com.winkar.Utils

import scala.xml._
/**
  * Created by winkar on 16-4-15.
  */
object HierarchyUtil {
  def getStructure(hierarchy: Elem) : Node ={
    <hierarchy>
      {hierarchy.child.filter(_.attribute("class").nonEmpty).map(getSubStructure)}
    </hierarchy>
  }

  def getSubStructure(elem: Node): Node = {
    <node class={elem.attribute("class")} resource-id={elem.attribute("resource-id")}>
      {elem.child.filter(_.attribute("class").nonEmpty).map(getSubStructure)}
    </node>

  }

  def uiStructureHashDigest(hierachy: String) = {
    MessageDigest.Md5(new xml.PrettyPrinter(80, 4).format(getStructure(XML.loadString(hierachy))))
  }
}
