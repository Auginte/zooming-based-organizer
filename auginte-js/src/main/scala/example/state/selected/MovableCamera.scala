package example.state.selected

import example.state.Position

case class MovableCamera(last: Position = Position()) extends Selected {
  def withPosition(position: Position) = copy(last = position)
}