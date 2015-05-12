package com.auginte.shared.state.selected

import com.auginte.shared.state.CameraId

case class SelectedCamera(cameraId: Option[CameraId] = None, movable: MovableCamera = MovableCamera(), zoomable: ZoomableCamera = ZoomableCamera()) {
  private val defaultCamera = 1

  def select = copy(cameraId = Some(defaultCamera))

  def deselect = copy(cameraId = None)

  def isSelected = cameraId.isDefined

  def inMovable(converter: MovableCamera => MovableCamera) = copy(movable = converter(movable))

  def inZoomable(converter: ZoomableCamera => ZoomableCamera) = copy(zoomable = converter(zoomable))
}