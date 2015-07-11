package com.auginte.scalajs

import com.auginte.scalajs.events.logic.mouse
import com.auginte.scalajs.events.logic.touch
import com.auginte.scalajs.proxy.ElementProxy
import japgolly.scalajs.react.vdom.{prefix_<^, Attr}
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * Object representing dragable element
 */
object Element extends SimpleComponent[ElementProxy]("Element") {
  override def generate(P: ElementProxy): prefix_<^.ReactTag = {
    val selected = if (P.element.selected) " selected-element" else ""

    <.span(
      P.element.text,
      ^.key := s"$componentName:${P.element.id}",
      ^.`class` := "dragable no-text-select" + selected,
      ^.left := (P.element.x - P.camera.x) / P.camera.scale,
      ^.top := (P.element.y - P.camera.y) / P.camera.scale,
      ^.width := P.element.width / P.camera.scale,
      ^.height := P.element.height / P.camera.scale,
      ^.fontSize := s"${1.0 / P.camera.scale}em",
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
