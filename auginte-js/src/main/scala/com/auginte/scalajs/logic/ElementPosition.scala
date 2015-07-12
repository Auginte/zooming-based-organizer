package com.auginte.scalajs.logic

import com.auginte.shared.state.persistable.{Position, Camera, Element}

/**
 * Position of rendered element
 */
case class ElementPosition(element: Element, camera: Camera) {
  val relativePrecision = 0.00001

  def x = (element.x - camera.x) / camera.scale
  def y = (element.y - camera.y) / camera.scale

  def width = element.width / camera.scale * element.scale
  def height = element.height / camera.scale * element.scale

  def relative: Double = {
    val precise = 1.0 / camera.scale * element.scale
    if (precise > relativePrecision) precise else 0.0
  }

  def center: Position = Position(width / 2 + x, height / 2 + y)

  def dimensionCenter(another: Element) = Position((width - another.width) / 2, (height - another.height) / 2)

  override def toString: String = s"ElementPosition($x x $y, $width x $height | $element, $camera)"
}
