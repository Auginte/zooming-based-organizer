package com.auginte.scalajs.state.selected

import com.auginte.scalajs.state.persistable.Position

trait Selected {
  val last: Position

  def difference(position: Position) = position - last
}