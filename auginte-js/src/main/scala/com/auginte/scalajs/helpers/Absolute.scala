package com.auginte.scalajs.helpers

import com.auginte.scalajs.CSSOMView
import com.auginte.shared.state.persistable.Position
import org.scalajs.dom

/**
 * Helper to get absolute position from HTML object
 */
object Absolute {
  def getAbsolute(element: dom.Node): Position = {
    try {
      val rect = element.asInstanceOf[CSSOMView].getBoundingClientRect()
      Position(rect.left, rect.top)
    } catch {
      case e: Exception => Position(0, 0)
    }
  }
}