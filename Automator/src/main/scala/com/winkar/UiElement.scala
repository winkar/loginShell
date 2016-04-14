package com.winkar

import io.appium.java_client.android.AndroidElement
import scala.collection.mutable




/**
  * Created by WinKaR on 16/3/22.
  */


object UiElement {
  def formatAndroidElement(elm:AndroidElement) : String =  s"Tag:${elm.getTagName};" +
    s"Text:${elm.getText};" +
    s"resourceId:${elm.getAttribute("resourceId")};" +
    s"contentDesc:${elm.getAttribute("name")};"

  def toUrl(view: String, elm: AndroidElement): String = {
    s"${view}_${formatAndroidElement(elm)}"
  }

  val visitedUrl = mutable.Set[String]()

  def urlVisited(url: String): Boolean = {
    if (visitedUrl.contains(url)) {
      true
    } else {
      visitedUrl.add(url)
      false
    }
  }

  val noClickTags = Array(
    "android.widget.EditText",
    "android.widget.Spinner"
  )

  val backRegex = Array(
    """.*[Bb]ack.*""".r,
    """.*nav_left.*""".r,
    """left_icon""".r,
    """返回""".r
  )

  val blackListRegex = Array(
    """否|([Nn]o)""".r,
    """[cC]lear""".r,
    """安装""".r,
    """[Ii]nstall""".r,
    """下载""".r,
    """[dD]ownload""".r,
    """下载""".r
  )
}

class UiElement(element: AndroidElement, view: String) {
  var androidElement: AndroidElement = element

  object Importance {
    val Default = "Default"
    val Significant = "Significant"
    val Trivial = "Trivial"
  }

  var significance: String = {
    Importance.Default
  }

  val id = element.getId
  val text = element.getText
  val tagName: String = element.getTagName
  val resourceId: String = element.getAttribute ("resourceId") match {
      case null => ""
      case s: String => s
    }


  val contentDesc: String = element.getAttribute("name")

  var parentView: ViewNode = null


  var willChangeCurrentUI: Boolean = false
  var isBack:  Boolean = List(resourceId, text, contentDesc).exists(isInBackRegex)
  val validTag: Boolean = !UiElement.noClickTags.contains(tagName)


  def isInBackRegex(s : String): Boolean = UiElement.backRegex.exists(_.findFirstIn(s).isDefined)
  def isInBlackList(s : String): Boolean = UiElement.blackListRegex.exists(_.findFirstIn(s).isDefined)

  val inBlackList : Boolean = List(resourceId, text, contentDesc).exists(isInBlackList)

  var destView: String = null
  var srcView: String = view
  val url = toString
  var clicked = false
//  val focusable = element.getAttribute("focusable")=="true"

  def click() = {
    element.click()
    parentView.elementsVisited(this) = true
    clicked = true
  }


//  def isEmpty = (text + resourceId).trim.isEmpty

  def shouldClick: Boolean = !inBlackList && !isBack && validTag &&     //Valid Check
    element.isDisplayed &&  // Display Check
    destView != srcView // Route Check

  def visited: Boolean = UiElement.urlVisited(this.toString) || clicked

  def visitComplete: Boolean = clicked && !parentView.parent.getNode(destView).visitComplete

  override def hashCode() = toString.hashCode
  override def equals(obj: Any) = obj match {
    case ele: UiElement =>
      ele.toString == toString
    case _ => false
  }


  override def toString: String = s"View:$srcView;  Tag:$tagName;" +
    s"Text:$text;" +
    s"resourceId:$resourceId;" +
    s"contentDesc:$contentDesc;"
}
