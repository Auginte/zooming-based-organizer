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

  def renderConnection(from: (Double, Double), to: (Double, Double), alpha: Double = 1.0): Unit = {
    val line = new ReferLine {
      start = from
      end = to
    }
    val fadeOutAlpha = inRange(alpha) / 10.0
    val stops = List(
      new Stop(1, withAlpha(coreColor, inRange(alpha))),
      new Stop(0, withAlpha(coreColor, inRange(fadeOutAlpha)))
    )
    val fadeOnEnd = new LinearGradient(from._1, from._2, to._1, to._2, false, CycleMethod.NO_CYCLE, stops)
    line.setStroke(fadeOnEnd)
    line.setFill(fadeOnEnd)
    d.getChildren.add(line)
  }

  private def withAlpha(color: Color, alpha: Double) = new Color(color.getRed, color.getGreen, color.getBlue, alpha)

  private def inRange(value: Double, min: Double = 0, max: Double = 1.0) =
    if (value > max) max else if (value < min) min else value

  def hideConnections(): Unit = {
    val connections = d.getChildren.filter{
      case e: ReferLine => true
      case _ => false
    }
    d.getChildren.removeAll(connections)
  }
}
