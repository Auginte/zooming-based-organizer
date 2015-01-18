package com.auginte.desktop.nodes

import javafx.scene.{layout => jfxl}

import com.auginte.common.Unexpected
import com.auginte.desktop.HaveOperations
import com.auginte.desktop.operations.ContextMenuWrapper
import com.auginte.desktop.persistable.{Container, View, ViewWrapper}
import com.auginte.desktop.rich.RichJPane
import com.auginte.distribution.orientdb.RepresentationWrapper

import scalafx.event.ActionEvent
import scalafx.scene.input.{MouseButton, MouseEvent}

/**
 * Common functionality among all data elements.
 *
 * E.g. show context menu, remove.
 */
trait Node extends RichJPane
with RepresentationWrapper
with ViewWrapper
with HaveOperations
{
  mouseClicked += {
    (e: MouseEvent) => if (e.button == MouseButton.SECONDARY) view match {
      case Some(v: ContextMenuWrapper) => v.showContextMenu(this)
      case _ => Unexpected.state(s"View does not have context menu: $view")
    }
  }

  override def operations: Operations = Map(
    "Delete" -> ((e: ActionEvent) => remove())
  )

  protected def remove(): Unit = view match {
    case Some(v: Container) => v.remove(this)
    case _ => storage.remove()
  }

  private def inView(f: View => Unit): Unit = view match {
    case Some(view) => f(view)
    case None =>
  }
}
