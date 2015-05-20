package com.auginte.shared

package object state {
  type CameraId = Int

  val viewId: Id = ""

  type Tr[A] = A => A

  type Id = String
}