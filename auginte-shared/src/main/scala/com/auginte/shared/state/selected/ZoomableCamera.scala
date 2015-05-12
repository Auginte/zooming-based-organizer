package com.auginte.shared.state.selected

import com.auginte.shared.state.persistable.Position

case class ZoomableCamera(last: Position = Position(), lastDistance: Double = -1, currentDistance: Double = -1) extends Selected {
  private val noScale = -1

  def withLastPosition(pos: Position) = copy(last = pos)

  def withLastDistance(distance: Double) = copy(lastDistance = distance)

  def withDistance(distance: Double) = copy(currentDistance = distance)

  def isZooming = lastDistance != noScale

  def cancelZooming = copy(lastDistance = noScale)
}