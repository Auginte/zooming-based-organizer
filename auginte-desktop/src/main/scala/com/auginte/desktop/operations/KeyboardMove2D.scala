package com.auginte.desktop.operations

import javafx.scene.{input => jfxi}
import javafx.{collections => jfxc, scene => jfxs}

import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.scene.{input => sfxi}

/**
 * Simple functionality to add new elements on cursor position when key is pressed
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait KeyboardMove2D[D <: jfxs.Node] extends Move2D[D] {
  protected val keyboardDragStep: Double

  keyPressed += {
    (e: KeyEvent) => if (e.altDown) e.code match {
      case KeyCode.W | KeyCode.UP => saveDraggedPosition(0, -keyboardDragStep)
      case KeyCode.S | KeyCode.DOWN => saveDraggedPosition(0, keyboardDragStep)
      case KeyCode.A | KeyCode.LEFT => saveDraggedPosition(-keyboardDragStep, 0)
      case KeyCode.D | KeyCode.RIGHT => saveDraggedPosition(keyboardDragStep, 0)
      case _ => Unit
    }
  }
}
