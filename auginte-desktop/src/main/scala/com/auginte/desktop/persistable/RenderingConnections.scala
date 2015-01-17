package com.auginte.desktop.persistable

import com.auginte.desktop.nodes.ReferLine
import com.auginte.desktop.rich.RichSPane

import scalafx.scene.input.MouseEvent
import scala.collection.JavaConversions._

/**
 * Drawing reference lines between persistable nodes.
 */
trait RenderingConnections extends RichSPane {
  def renderConnection(from: (Double, Double), to: (Double, Double)): Unit = {
    val line = new ReferLine {
      start = from
      end = to
    }
    d.getChildren.add(line)
  }

  def hideConnections(): Unit = {
    val connections = d.getChildren.filter{
      case e: ReferLine => true
      case _ => false
    }
    d.getChildren.removeAll(connections)
  }
}
