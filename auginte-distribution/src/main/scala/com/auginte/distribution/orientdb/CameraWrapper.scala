package com.auginte.distribution.orientdb

/**
 * Wraps GUI view to have links to camera and representations
 */
trait CameraWrapper {
  private var _camera: Option[Camera] = None

  def camera = _camera

  def camera_=(camera: Camera): Unit = _camera = Some(camera)
}
