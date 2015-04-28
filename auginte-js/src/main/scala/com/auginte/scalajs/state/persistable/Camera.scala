package com.auginte.scalajs.state.persistable

/**
 * Data structure, representing view transformation
 */
case class Camera(x: Double = 0, y: Double = 0, scale: Double = 1) {
  def withPosition(x: Double, y: Double) = copy(x = x, y = y)

  def withScale(scale: Double) = copy(scale = scale)

  def moved(difference: Position) =  copy(x = x + difference.x, y = y + difference.y)

  def and(converter: Camera => Camera) = converter(this)
}