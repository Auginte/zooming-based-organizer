package com.auginte.scalajs.state.selected

import com.auginte.scalajs.state.persistable.Position

case class MovableCamera(last: Position = Position()) extends Selected {
  def withPosition(position: Position) = copy(last = position)
}