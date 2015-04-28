package com.auginte.scalajs

/**
 * Data structures to store sate and do simple data manipulation
 */
package object state {
  type CameraId = Int

  val viewId: Id = -1

  /**
   * Transforming GUI state object
   */
  type T = State => State

  type Tr[A] = A => A

  type Id = Int

}
