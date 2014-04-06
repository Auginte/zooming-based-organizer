package com.auginte.desktop

import scalafx.scene.layout.Pane
import scalafx.scene.input.ScrollEvent
import com.auginte.desktop.actions.AddWithMouse
import scalafx.scene.input.MouseEvent
import scalafx.Includes._

/**
 * JavaFX panel with Infinity zooming layout.
 * Can share content with over Views.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class View extends Pane with RichNode with AddWithMouse {

  //FIMXE: Debug info
  onScroll = ((e: ScrollEvent) => println("Wheel", e.getDeltaX, e.getDeltaY))
  mouseEvents += ((e: MouseEvent) => println("Clicked", e.x, e.y))

}
