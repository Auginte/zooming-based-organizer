package com.auginte.scalajs.events.logic.touch

import japgolly.scalajs.react.ReactTouchEvent

/**
 * Starting dragging
 */
case class DragBegin(reactTouchEvent: ReactTouchEvent) extends TouchEvent
