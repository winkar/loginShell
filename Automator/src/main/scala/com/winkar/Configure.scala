package com.winkar

import java.io.File
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

