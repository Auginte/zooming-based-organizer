package com.auginte.shared

package object state {
  type CameraId = Int

  val viewId: Id = -1

  type Tr[A] = A => A

  type Id = Int
}