package com.auginte.desktop.operations

import com.auginte.desktop.rich.RichNode
import javafx.{scene => jfxs}
import scalafx.scene.input.{MouseEvent, KeyCode, KeyEvent, ScrollEvent}

/**
 * Functionality to zoom elements with mouse
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait KeyboardZoom[D <: jfxs.Node] extends RichNode[D] with Zoom {
  protected val keyboardZoomStep: Double = 10

  private var lastX: Double = 0
  private var lastY: Double = 0

  mouseMoved += {
    (e: MouseEvent) => {
      lastX = e.x
      lastY = e.y
    }
  }

  keyPressed += {
    (e: KeyEvent) => if (e.altDown) e.code match {
      case KeyCode.Q  => zoomed(delta2scale(keyboardZoomStep), lastX, lastY)
      case KeyCode.E  => zoomed(delta2scale(-keyboardZoomStep), lastX, lastY)
      case _ => Unit
    }
  }
}
