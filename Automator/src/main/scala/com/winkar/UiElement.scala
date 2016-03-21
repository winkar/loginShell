package com.winkar

import io.appium.java_client.android.AndroidElement




/**
  * Created by WinKaR on 16/3/22.
  */
class UiElement(element: AndroidElement, currentActivity: UIActivity) {
  var androidElement: AndroidElement = element

  object Importance {
    val Default = "Default"
    val Significant = "Significant"
    val Trivial = "Trivial"
  }

  var significance: String = {
    Importance.Default
  }

  var willChangeCurrentUI: Boolean = false
  var isBack:  Boolean = false

  val id = element.getId
  val text = element.getText
  val tagName = element.getTagName
  val resourceId = element.getAttribute("resourceId")

  var destActivity: UIActivity = null
  var srcActivity: UIActivity = currentActivity
  val url: String = {
    s"${srcActivity}_${toString}"
  }
  var clicked = false


  def click = {
    element.click
    clicked = true
  }

  override def toString: String = String.format(s"Tag: ${tagName}; " +
    s"Id ${id}; " +
    s"Text ${text}; " +
    s"resourceId ${resourceId}")
}
