package com.winkar

import com.winkar.Appium.AppiumServer

/**
  * Created by winkar on 16-4-6.
  */

object Configure {
  val SingleAppTest : String = "SingleAppTest"
  val MultiAppTest : String = "MultiAppTest"
  val Modes = List(SingleAppTest, MultiAppTest)
}

object GlobalConfig {
  var fast: Boolean = true
  var currentPackage = ""
  var server: AppiumServer = null
}

case class Configure(
                      mode: String = "SingleAppTest",
                      apkFileList: Seq[String] = null,
                      apkFile: String = null,
                      configFile: String = null,
                      apkDirectory: String = "",
//                      verbose: Boolean,
                      fast : Boolean = true,
                      kwargs: Map[String,String] = Map()
                    )

