package com.winkar

import io.appium.java_client.android.AndroidElement
import scala.collection.mutable




/**
  * Created by WinKaR on 16/3/22.
  */


object UiElement {
  def formatAndroidElement(elm:AndroidElement) : String =  s"Tag: ${elm.getTagName}; " +
    s"Text ${elm.getText}; " +
    s"resourceId ${elm.getAttribute("resourceId")}"

  def toUrl(activity: String, elm: AndroidElement): String = {
    s"${activity}_${formatAndroidElement(elm)}"
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


}

class UiElement(element: AndroidElement, activity: String) {
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

  var willChangeCurrentUI: Boolean = false
  var isBack:  Boolean = backRegex.exists(_.findFirstIn(resourceId).isDefined)
  val validTag: Boolean = !noClickTags.contains(tagName)


  def isInBlackList(s : String): Boolean = blackListRegex.exists(_.findFirstIn(s).isDefined)

  val inBlackList : Boolean = List(resourceId, text).exists(isInBlackList)

  var destActivity: String = null
  var srcActivity: String = activity
  val url = toString
  var clicked = false
//  val focusable = element.getAttribute("focusable")=="true"

  def click = {
    element.click
    clicked = true
  }


//  def isEmpty = (text + resourceId).trim.isEmpty

  def shouldClick: Boolean = !inBlackList && !clicked && !isBack && validTag && !UiElement.urlVisited(this.toString)


  override def toString: String = String.format(s"Tag:${tagName};" +
    s"Text:${text};" +
    s"resourceId:${resourceId};" +
    s"contentDesc:${contentDesc};")
}
