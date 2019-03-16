package com.auginte.desktop.utilities

import java.text.{DecimalFormat, SimpleDateFormat}
import java.util.Date

object Logger {
  private def time: String = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date())

  private def format (action: => Unit, name: String): Unit = {
    val t0 = System.nanoTime()
    System.out.println(s"$time: $name started")
    action
    val took = System.nanoTime() - t0
    System.out.println(s"$time: $name finished. Took: $took")
  }

  def loadingFile(action: => Unit): Unit = if (Config.logStorage) format(action, "Loading file") else action
  def loadingFromStream(action: => Unit): Unit = if (Config.logStorage) format(action, " Loading from stream") else action
  def save(action: => Unit): Unit = if (Config.logStorage) format(action, "Storing") else action
}
