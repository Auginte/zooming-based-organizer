package com.auginte.scalajs.events.logic.touch

import japgolly.scalajs.react.ReactTouchEvent

/**
 * Dragging in progress
 */
case class Drag(reactTouchEvent: ReactTouchEvent) extends TouchEvent
