package com.winkar

/**
  * Created by WinKaR on 16/3/31.
  */
object Tapper {
implicit def anyToTapper[A](obj: A): Tapper[A] = new Tapper(obj)
}

class Tapper[A](obj: A) {
  def tap(code: A => Unit): A = {
    code(obj)
    obj
  }
}