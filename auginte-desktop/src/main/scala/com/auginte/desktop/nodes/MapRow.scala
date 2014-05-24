package com.auginte.desktop.nodes

import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer

/**
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class MapRow(key_ : String, value_ : String) {
  val key = new StringProperty(this, "key", key_)
  val value = new StringProperty(this, "value", value_)

  def getKey = key.value
  def getValue = value.value
}

object MapRow {
  def fromMap(map: Map[_ <: Any, _ <: Any]): ObservableBuffer[MapRow] = {
    val buffer = new ObservableBuffer[MapRow]()
    buffer.insertAll(0, for (m <- map) yield new MapRow(m._1.toString, m._2.toString))
    buffer
  }
}
