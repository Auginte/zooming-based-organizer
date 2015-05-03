package com.auginte.scalajs

import com.auginte.scalajs.events.logic.mouse
import com.auginte.scalajs.events.logic.touch
import com.auginte.scalajs.proxy.ViewProxy
import japgolly.scalajs.react.vdom.{Attr, prefix_<^}
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * Displaying elements with camera transformation
 */
object View extends SimpleComponent[ViewProxy]("View") {
  override def generate(P: ViewProxy): prefix_<^.ReactTag =
    <.div(
      ^.`class` := "area",
      <.div(
        P.elements(Element.generate, P.state.camera)
      ),
      ^.onMouseDown ==> P.mouseReceive(mouse.DragBegin),
      ^.onMouseMove ==> P.mouseReceive(mouse.Drag),
      ^.onMouseUp ==> P.mouseReceive(mouse.DragEnd),
      Attr("onWheel") ==> P.wheelReceive(mouse.Wheel),
      ^.onTouchStart ==> P.touchReceive(touch.DragBegin),
      ^.onTouchMove ==> P.touchReceive(touch.Drag),
      ^.onTouchEnd ==> P.touchReceive(touch.DragEnd),
      ^.onTouchCancel ==> P.touchReceive(touch.DragCancel)
    )
}
