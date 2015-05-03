package com.auginte.scalajs.events.logic.touch

import japgolly.scalajs.react.ReactTouchEvent

/**
 * Finishing dragging because of context menu or other not natural event
 */
case class DragCancel(reactTouchEvent: ReactTouchEvent) extends TouchEvent
