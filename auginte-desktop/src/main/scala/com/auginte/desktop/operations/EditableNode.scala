package com.auginte.desktop.operations

/**
 * Switching between edit/view mode.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait EditableNode {
  def editable_=(value: Boolean): Unit

  def editable: Boolean
}
