package com.auginte.desktop.events

import javafx.scene.{layout => jfxl}

import com.auginte.desktop.operations.EditableNode

/**
 * Toggle edit mode.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class EditElement(editable: EditableNode, mode: Boolean)
