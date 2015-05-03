package com.auginte.scalajs.events.logic.touch

import japgolly.scalajs.react.ReactTouchEvent

/**
 * Finishing dragging
 */
case class DragEnd(reactTouchEvent: ReactTouchEvent) extends TouchEvent
