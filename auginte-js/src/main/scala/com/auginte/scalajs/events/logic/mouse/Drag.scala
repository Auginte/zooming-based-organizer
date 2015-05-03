package com.auginte.scalajs.events.logic.mouse

import com.auginte.scalajs.events.PointerEvent

/**
 * Dragging in progress
 */
case class Drag(reactMouseEvent: PointerEvent) extends MouseEvent
