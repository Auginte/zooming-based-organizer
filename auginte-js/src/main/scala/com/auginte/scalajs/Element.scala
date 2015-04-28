package com.auginte.scalajs

import com.auginte.scalajs.events.PointerEvent
import com.auginte.scalajs.helpers.Proxy
import com.auginte.scalajs.state.Camera
import japgolly.scalajs.react.vdom.Attr
import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * Object representing dragable element
 */
object Element {
  val render = ReactComponentB[Proxy[PointerEvent, state.Element, Camera]]("Element")
  .stateless
  .noBackend
  .render { (P, S, B) =>

    <.span(
      P.element.text,
      ^.`class` := "dragable noselect",
      ^.left := (P.element.x - P.camera.x) / P.camera.scale,
      ^.top := (P.element.y - P.camera.y) / P.camera.scale,
      ^.width := P.element.width / P.camera.scale,
      ^.height := P.element.height / P.camera.scale,
      ^.fontSize := s"${1.0 / P.camera.scale}em",
      ^.onMouseDown ==> P.receive,
      ^.onMouseUp ==> P.receive,
      ^.onMouseMove ==> P.receive,
      Attr("data-element-id") := P.element.id,
      ^.onTouchStart ==> P.receive,
      ^.onTouchMove ==> P.receive,
      ^.onTouchEnd ==> P.receive,
      ^.onTouchCancel ==> P.receive
    )

  }.build
}
