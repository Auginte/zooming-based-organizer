package example.helpers

import example.events.ScreenPosition
import example.state._
import org.scalajs.dom
import scala.annotation.tailrec

/**
 * Helpers for Dom manipulation related functionality
 */
object Dom {
  def getData(element: dom.Node, dataAttribute: String): Option[String] = {
    val value = attribute(element, s"data-$dataAttribute", "")
    if (value != "") Some(value) else None
  }

  @tailrec def findView(children: dom.Node): dom.Node = {
    val reached = attribute(children, "class") == "area"
    val top = children.nodeName == "body" || children.parentNode == null
    if (reached || top) children else findView(children.parentNode)
  }

  def attribute(node: dom.Node, name: String, default: String = "") = try {
    if (node != null && !node.isInstanceOf[dom.Document] && node.attributes != null && node.attributes.getNamedItem(name) != null) {
      node.attributes.getNamedItem(name).value
    } else {
      default
    }
  } catch {
    case e: Exception => default
  }

  def position(screenPos: ScreenPosition) = Position(screenPos.screenX, screenPos.screenY)
}
