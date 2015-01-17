package com.auginte.desktop.nodes

import javafx.scene.shape.Line

class ReferLine extends Line {
  type Position2D = (Double, Double)

  def start: Position2D = (getStartX, getStartY)
  def end: Position2D = (getEndX, getEndY)
  
  def start_=(xy: Position2D): Unit = {
    setStartX(xy._1)
    setStartY(xy._2)
  }
  def end_=(xy: Position2D): Unit = {
    setEndX(xy._1)
    setEndY(xy._2)
  }
}
