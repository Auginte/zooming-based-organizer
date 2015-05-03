package com.auginte.scalajs

import com.auginte.scalajs.events.logic.{SaveName, Add, ToggleSidebar}
import com.auginte.scalajs.proxy.{CreationProxy, SidebarProxy}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.{Attr, prefix_<^}
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * Form to aqdd new elements
 */
object Creation extends SimpleComponent[CreationProxy]("Creation") {

  override def generate(P: CreationProxy): prefix_<^.ReactTag =
    <.div(
      ^.ref := componentName,
      ^.`class` := "input-bar",
      <.form(
        ^.onSubmit ==> P.receive(Add()),
        <.input(
          ^.onChange ==> P.inputReceive(e => SaveName(e.target.value)),
          ^.value := P.state.name,
          ^.`class` := "form-control input-field",
          Attr("placeholder") := "New element"
        ),
        <.button(
          <.span("+", ^.`class` := "short"),
          <.span("+", ^.`class` := "long"),
          <.span("Add", ^.`class` := "longest"),
          ^.`class` := "btn btn-add"
        )
      )
    )

}
