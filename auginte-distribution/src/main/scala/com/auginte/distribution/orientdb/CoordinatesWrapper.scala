package com.auginte.distribution.orientdb

import com.auginte.zooming.Coordinates

/**
 * Having x,y,scale parameters compatible with Infinity zooming Distance data structure.
 */
trait CoordinatesWrapper {
  def x: Double

  def y: Double

  def scale: Double

  def x_=(x: Double)

  def y_=(y: Double)

  def scale_=(scale: Double)

  def coordinates: Coordinates = Coordinates(x, y, scale)

  def coordinates_=(d: Coordinates): Unit = {
    x = d.x
    y = d.y
    scale = d.scale
  }
}
