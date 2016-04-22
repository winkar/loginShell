package com.winkar.Utils

import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._

/**
  * Created by winkar on 16-4-20.
  */
class Timer {
  var startTime: DateTime = null
  var stopTime: DateTime = null

  def start = {
    startTime  = DateTime.now()
    this
  }

  def stop = {
    stopTime = DateTime.now()
    startTime to stopTime toPeriod
  }
}
