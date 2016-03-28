package com.winkar


import scala.collection.mutable.Map

/**
  * Created by WinKaR on 16/3/23.
  */
class UIActivity(activityName: String) {
  val activity: String = activityName
  val clickCompleted: Boolean = false

  val clickableUiElements = Map[String, UiElement]()
}
