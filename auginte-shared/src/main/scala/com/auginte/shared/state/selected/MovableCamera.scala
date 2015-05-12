package com.auginte.shared.state.selected

import com.auginte.shared.state.persistable.Position

case class MovableCamera(last: Position = Position()) extends Selected {
  def withPosition(position: Position) = copy(last = position)
}