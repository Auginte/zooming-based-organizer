package com.auginte.scalajs

import com.auginte.scalajs.events.logic.mouse
import com.auginte.scalajs.events.logic.touch
import com.auginte.scalajs.logic.ElementPosition
import com.auginte.scalajs.proxy.ElementProxy
import japgolly.scalajs.react.vdom.{prefix_<^, Attr}
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * Object representing dragable element
 */
object Element extends SimpleComponent[ElementProxy]("Element") {
  override def generate(P: ElementProxy): prefix_<^.ReactTag = {
    val selected = if (P.element.selected) " selected-element" else ""
    val pos = ElementPosition(P.element, P.camera)

    <.span(
      P.element.text,
      ^.key := s"$componentName:${P.element.id}",
      ^.`class` := "dragable no-text-select" + selected,
      ^.left := pos.x,
      ^.top := pos.y,
      ^.width := pos.width,
      ^.height := pos.height,
      ^.fontSize := s"${pos.relative}em",
      ^.onMouseDown ==> P.mouseReceive(mouse.DragBegin),
      ^.onMouseMove ==> P.mouseReceive(mouse.Drag),
      ^.onMouseUp ==> P.mouseReceive(mouse.DragEnd),
      Attr("data-element-id") := P.element.id,
      ^.onTouchStart ==> P.touchReceive(touch.DragBegin),
      ^.onTouchMove ==> P.touchReceive(touch.Drag),
      ^.onTouchEnd ==> P.touchReceive(touch.DragEnd),
      ^.onTouchCancel ==> P.touchReceive(touch.DragCancel)
    )
  }
}
