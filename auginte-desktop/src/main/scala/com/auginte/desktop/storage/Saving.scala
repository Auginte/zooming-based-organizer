package com.auginte.desktop.storage

import java.io.FileOutputStream
import javafx.scene.{layout => jfxl}

import com.auginte.desktop.actors.Container
import com.auginte.desktop.zooming.ZoomableElement
import com.auginte.distribution.data.{Camera, Data}
import com.auginte.distribution.repository._
import com.auginte.zooming.Grid

import scala.collection.JavaConversions._

/**
 * Functionality for loading new project.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Saving[D <: jfxl.Pane] extends UsingRepository
with Container[D]
with Camera with ZoomableElement {
  private val elements: Elements = () => d.getChildren flatMap { case d: Data => Some(d) case _ => None}
  private val cameras: Cameras = () => List(this)

  private val gridCreator = (grid: Grid) => new Grid {
    override def root = grid.root
  }

  protected def save(path: String): Unit = {
    val stream = new FileOutputStream(path)
    repository.save(stream, grid, elements, cameras)
    stream.close()
  }
}
