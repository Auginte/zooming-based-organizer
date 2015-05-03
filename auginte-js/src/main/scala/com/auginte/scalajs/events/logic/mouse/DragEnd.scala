package com.auginte.scalajs.events.logic.mouse

import com.auginte.scalajs.events.PointerEvent

/**
 * Finishing dragging
 */
case class DragEnd(reactMouseEvent: PointerEvent) extends MouseEvent
