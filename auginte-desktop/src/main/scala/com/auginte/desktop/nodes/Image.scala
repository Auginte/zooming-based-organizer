package com.auginte.desktop.nodes

import javafx.scene.{image => jfxi}
import javafx.scene.image.{ImageView => iv}
import com.auginte.desktop.HaveOperations
import com.auginte.desktop.actors.{ScalableElement, DragableNode, ViewableNode}
import com.auginte.desktop.events.{ImportElement, DeleteElement}
import com.auginte.desktop.operations.{EditableNode, MouseTransformable}
import com.auginte.desktop.rich.{RichImageView, RichJPane}
import com.auginte.desktop.zooming.ZoomableNode
import com.auginte.distribution.data.Data
import com.auginte.transforamtion.Transformable

import scalafx.event.ActionEvent

/**
 * Representation with picture.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Image extends RichImageView
with ViewableNode with HaveOperations with DragableNode[iv] with ZoomableNode[iv] with ScalableElement[iv]
with Data
with Transformable[Image] with MouseTransformable[iv]
{
  override def operations: Operations = Map(
    "Delete" -> ((e: ActionEvent) => view ! DeleteElement(this))
  )

  /**
   * Create independent copy of element
   *
   * @return Deep cloned object
   */
  override protected def createCloned(): Unit = {
    val clone = transformed(cloneParameters).original
    clone.swapSources(this)
    view ! ImportElement(clone)
  }

  override protected def copy: Image = {
    val image = new Image
    image.node = node
    image.position = position.clone()
    image
  }

  val image = new jfxi.Image(getClass.getResourceAsStream("/com/auginte/common/splash.gif"))
  d.setImage(image)
}
