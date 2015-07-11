package com.auginte.scalajs

import com.auginte.scalajs.events.logic.DeleteSelected
import com.auginte.scalajs.proxy.SelectedOperationsProxy
import japgolly.scalajs.react.vdom.prefix_<^
import japgolly.scalajs.react.vdom.prefix_<^._

/**
 * Operations for selected items
 */
object SelectedOperations extends SimpleComponent[SelectedOperationsProxy]("SelectedOperations") {
  def generate(P: SelectedOperationsProxy): prefix_<^.ReactTag = {
    if (P.hasSelectedElements) {
      <.div(
        ^.`class` := "selected-sidebar",
        <.div(
          ^.`class` := "title",
          "Selected element"
        ),
        <.button(
          "Delete",
          ^.`class` := "acte-delete-selected",
          ^.onClick ==> P.receive(DeleteSelected())
        )
      )
    } else {
      <.div()
    }
  }
}
