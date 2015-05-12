package com.auginte.shared.state.selected

import com.auginte.shared.state.persistable.Position

trait Selected {
  val last: Position

  def difference(position: Position) = position - last
}