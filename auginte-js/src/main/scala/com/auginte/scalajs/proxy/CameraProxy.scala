package com.auginte.scalajs.proxy

/**
 * Callback data structure for camera elements
 *
 * @tparam Parameters Immutable data for child (parameters)
 * @tparam Context    Immutable data structure of parent class (context)
 * @tparam Camera     Logic class for callbacks
 */
trait CameraProxy[Parameters, Context, Camera] {
  def receive(data: Parameters)(implicit sender: Context = element): Unit

  def element: Context
  
  def camera: Camera
}
