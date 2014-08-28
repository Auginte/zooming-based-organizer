package com.auginte.desktop.actors

import com.auginte.desktop
import akka.actor.{Props, Actor}
import com.auginte.zooming.Grid

/**
 * Actor to manage data integrity over many Views.
 *
 * Creates child actors for each new GUI View object.
 *
 * @see [[com.auginte.desktop.actors.View]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Views extends Actor {
  private var gridOption: Option[Grid] = None

  private def grid: Grid = if (gridOption.isDefined) {
    gridOption.get
  } else {
    throw new IllegalArgumentException("Creating view without Grid passed")
  }

  override def receive = {
    case g: Grid => gridOption = Some(g)
    case view: desktop.View =>
      val child = context.actorOf(Props[View], s"view${context.children.size}")
      child ! view
      view.grid = grid
      view.node = grid.root
    case _ => Unit
  }
}
