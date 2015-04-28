package com.auginte.scalajs.state.selected

import com.auginte.scalajs.state.Position

trait Selected {
  val last: Position

  def difference(position: Position) = position - last
}