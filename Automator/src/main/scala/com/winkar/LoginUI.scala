package com.winkar

/**
  * Created by WinKaR on 16/3/29.
  */
object LoginUI {
  val loginRegex = Array(
    "登陆".r,
    "[lL]ogin".r
  )

  def isLoginUI(lastClickedElement :UiElement, elementsOnActivity: List[UiElement], activityName : String) : Boolean = {

    var elementsToCheck: List[String] = activityName ::
                        elementsOnActivity.flatMap((elm: UiElement) => List[String](elm.resourceId, elm.text))


    if (lastClickedElement != null) {
      elementsToCheck = lastClickedElement.text :: elementsToCheck
    }


    elementsToCheck.exists((s: String) => loginRegex.exists(_.findFirstIn(s).isDefined))
  }
}
