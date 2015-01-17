package com.auginte.desktop.persistable

import javafx.scene.paint.{Color, Stop, CycleMethod, LinearGradient}

import com.auginte.desktop.nodes.ReferLine
import com.auginte.desktop.rich.RichSPane

import scala.collection.JavaConversions._

/**
 * Drawing reference lines between persistable nodes.
 */
trait RenderingConnections extends RichSPane {
  private val coreColor = Color.valueOf("#d3f0ff")
  private val fadedOutColor = new Color(coreColor.getRed, coreColor.getGreen, coreColor.getBlue, 0.1)

  def renderConnection(from: (Double, Double), to: (Double, Double)): Unit = {
    val line = new ReferLine {
      start = from
      end = to
    }
    val stops = List(new Stop(1.0, coreColor), new Stop(0, fadedOutColor))
    val fadeOnEnd = new LinearGradient(from._1, from._2, to._1, to._2, false, CycleMethod.NO_CYCLE, stops)
    line.setStroke(fadeOnEnd)
    line.setFill(fadeOnEnd)
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
