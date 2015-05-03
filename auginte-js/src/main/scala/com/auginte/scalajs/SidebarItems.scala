package com.auginte.scalajs

import com.auginte.scalajs.events.logic.Save
import com.auginte.scalajs.proxy.EventProxy
import japgolly.scalajs.react.vdom.prefix_<^
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * Items, that are usually showed collapsed
 */
object SidebarItems extends SimpleComponent[EventProxy]("SidebarItems") {
  def generate(P: EventProxy): prefix_<^.ReactTag = {
    <.div(
      ^.ref := componentName,
      ^.`class` := "sidebar-items",
      <.button(
        "Save",
        ^.onClick ==> P.receive(Save())
      ),
      <.div(
        "Auginte v0.8.1"
      )
    )
  }
}
