package com.auginte.scalajs

import japgolly.scalajs.react._

package object events {
  /**
   * Common functionality of MouseEvent and Touch in TouchEvent
   */
  type PointerEvent = ReactMouseEvent with ScreenPosition with ClientPosition with EventFlow
}
