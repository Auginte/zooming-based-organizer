package com.auginte.scalajs

import com.auginte.scalajs.events.logic.Save
import com.auginte.scalajs.proxy.EventProxy
import com.auginte.shared.Version
import japgolly.scalajs.react.vdom.prefix_<^
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * Items, that are usually showed collapsed
 */
object SidebarItems extends SimpleComponent[EventProxy]("SidebarItems") {
  def title(name: String) = <.div(name, ^.`class` := "title")

  val version = Version.textual

  def generate(P: EventProxy): prefix_<^.ReactTag = {
    <.div(
      ^.ref := componentName,
      <.div(
        title("View"),
        <.button(
          "Save",
          ^.`class` := "acte-save",
          ^.onClick ==> P.receive(Save())
        )
      ),
      <.div(
        title("About"),
        <.a(
          s"Auginte v$version",
          ^.href := s"""mailto:"aurelijus@auginte.com"?subject=v$version"""
        )
      )
    )
  }
}
