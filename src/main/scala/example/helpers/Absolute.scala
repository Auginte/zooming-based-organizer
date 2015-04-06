package example.helpers

import example.CSSOMView
import example.state.Position
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