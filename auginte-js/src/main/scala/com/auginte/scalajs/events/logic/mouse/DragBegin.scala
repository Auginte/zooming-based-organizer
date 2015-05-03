package com.auginte.scalajs.events.logic.mouse

import com.auginte.scalajs.events.PointerEvent

/**
 * Starting dragging
 */
case class DragBegin(reactMouseEvent: PointerEvent) extends MouseEvent
