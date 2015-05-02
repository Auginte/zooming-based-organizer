package com.auginte.scalajs

import com.auginte.scalajs.events.logic.ToggleSidebar
import com.auginte.scalajs.proxy.SidebarProxy
import japgolly.scalajs.react.vdom.prefix_<^
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * More controls for User interface usually collapsed to sidebar
 */
object Sidebar extends SimpleComponent[SidebarProxy]("Sidebar") {
  def generate(P: SidebarProxy): prefix_<^.ReactTag = {
    val visibilityClass = if (P.state.exapnded) "expanded" else "collapsed"

    <.div(
      ^.`class` := "sidebar",
      <.button(
        if (P.state.exapnded) "<" else ">",
        ^.`class` := s"toggle $visibilityClass",
        ^.onClick ==> P.receive(ToggleSidebar())
      ),
      <.div(
        ^.`class` := visibilityClass,
        SidebarItems.generate(P)
      )
    )
  }
}
