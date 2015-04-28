package com.auginte.scalajs.state

/**
 * 2D position in the plane
 */
case class Position(x: Double = 0, y: Double = 0) {
  def *(scalar: Double) = Position(x * scalar, y * scalar)

  def /(scalar: Double) = Position(x / scalar, y / scalar)

  def +(pos: Position) = Position(x + pos.x, y + pos.y)

  def -(pos: Position) = Position(x - pos.x, y - pos.y)

  def unary_- = Position(-x, -y)

  def distance(to: Position): Double = math.sqrt((x - to.x) * (x - to.x) + (y - to.y) * (y - to.y))

  def center(to: Position) = Position((x + to.x) / 2.0, (y + to.y) / 2.0)
}