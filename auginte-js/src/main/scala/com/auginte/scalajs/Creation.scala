package com.auginte.scalajs

import com.auginte.scalajs.events.logic.{SaveName, Add, ToggleSidebar}
import com.auginte.scalajs.proxy.{CreationProxy, SidebarProxy}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * Form to aqdd new elements
 */
object Creation extends SimpleComponent[CreationProxy]("Creation") {

  override def generate(P: CreationProxy): prefix_<^.ReactTag = <.div(
    <.form(
      ^.ref := componentName,
      ^.onSubmit ==> P.receive(Add()),
      <.input(
        ^.onChange ==> inputEvent { e => P.receive(SaveName(e.target.value))(e) },
        ^.value := P.state.name,
        ^.`class` := "new-name"
      ),
      <.button("Add")
    )
  )

}
