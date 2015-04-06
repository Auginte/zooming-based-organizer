package example.state.selected

import example.state.Position

trait Selected {
  val last: Position

  def difference(position: Position) = position - last
}