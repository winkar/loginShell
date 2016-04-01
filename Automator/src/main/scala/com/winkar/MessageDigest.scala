package com.winkar

/**
  * Created by WinKaR on 16/3/31.
  */
object MessageDigest {
  def Md5(s: String) = java.security.MessageDigest.getInstance("MD5").digest(s.getBytes).map("%02X".format(_)).mkString("")
}
