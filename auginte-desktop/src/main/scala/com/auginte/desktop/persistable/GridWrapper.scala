package com.auginte.desktop.persistable

import com.auginte.zooming.{Node, Grid}

/**
 * Functionality witch needs links to grid
 */
trait GridWrapper {
  private var _grid: Option[Grid] = None

  def grid = _grid

  def grid_=(g: Grid): Unit = _grid = Some(g)
}
