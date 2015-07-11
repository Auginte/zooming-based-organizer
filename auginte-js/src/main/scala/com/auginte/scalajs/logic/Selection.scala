package com.auginte.scalajs.logic

import com.auginte.scalajs.state.T
import com.auginte.shared.state.persistable.Element

object Selection {
  def selectElement(element: Element): T = deselect andThen {
    _.inContainer(
      container => container.copy(
        elements = container.elements.updated(element.id, element.copy(selected = true))
      )
    )
  }

  private def deselect: T = _.inContainer {
    c => c.copy(elements = c.elements map {
      case (id, key) => id -> key.copy(selected = false)
    })
  } and {
    _.inCamera(_.copy(selected = false))
  }

  def selectCamera: T = deselect andThen {
    _.inCamera(_.copy(selected = true))
  }

  def withoutSelectedElement: T = _.inContainer(
    container => container.copy(
      elements = container.elements.filterNot(_._2.selected)
    )
  )
}
