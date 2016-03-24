package com.winkar

import scala.sys.process._


object AndroidUtils {
  def getPackageName(apkPath: String): String = s"scripts/getPackageName.sh ${apkPath}".!!.trim

  def getCurrentPackage() = "scripts/getCurrentPackage.sh".!!.trim
}
