package com.auginte.shared.state.selected

import com.auginte.shared.state.Id
import com.auginte.shared.state.persistable.{Position, Container}

case class SelectedElements(id: Option[Id] = None, last: Position = Position()) extends Selected {
  def withSelected(id: Id) = copy(id = Some(id))

  def deselect = copy(id = None)

  def withPosition(position: Position) = copy(last = position)

  def element(container: Container) = id match {
    case Some(elementId) => container.elements.get(elementId)
    case None => None
  }
}