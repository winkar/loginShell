package com.winkar

import org.apache.log4j.Logger


class SingleAppTester private[winkar](val apkPath: String) {
  val log: Logger = Logger.getLogger(Automator.getClass.getName)
  def startTest: Unit = {
    new AppTraversal(apkPath).start
  }
}
