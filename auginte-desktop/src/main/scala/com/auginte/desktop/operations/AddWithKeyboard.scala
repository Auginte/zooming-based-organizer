package com.auginte.desktop.operations

import javafx.scene.{input => jfxi}
import javafx.{collections => jfxc, scene => jfxs}

import com.auginte.desktop.rich.RichNode

import scalafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import scalafx.scene.{input => sfxi}

/**
 * Simple functionality to add new elements on cursor position when key is pressed
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait AddWithKeyboard[D <: jfxs.Node] extends RichNode[D] with InsertingElements[D] {
  private var lastMouseX: Double = 0
  private var lastMouseY: Double = 0

  mouseMoved += {
    (e: MouseEvent) => {
      lastMouseX = e.x
      lastMouseY = e.y
    }
  }

  keyReleased += {
    (e: KeyEvent) => if (isAddKey(e.code)) insertElement(createNewElement, lastMouseX, lastMouseY)
  }

  protected def isAddKey(code: KeyCode) = {
    code == KeyCode.SPACE
  }
}
