package example

import japgolly.scalajs.react.React
import scala.scalajs.js
import org.scalajs.dom

/**
 * Launching React application in Dom element
 */
object Main extends js.JSApp {
  def main(): Unit = {
    React.initializeTouchEvents(shouldUseTouch = true)
    React.render(DragableElements.render(), dom.document.getElementById("playground"))
  }
}
