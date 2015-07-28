package com.auginte.scalajs.logic

import com.auginte.shared.state.persistable.{Camera, Element, Position}

/**
 * Position transformations of view to rendered
 */
case class ViewPosition(camera: Camera, width: Int, height: Int) {
  private def renderedCetner = Position(width / 2.0, height / 2.0)

  def viewCenter = renderedCetner * camera.scale + camera.position
}
