package com.auginte.desktop.actors

import com.auginte.desktop
import akka.actor.Actor
import com.auginte.desktop.events.{InsertElement, MoveElement, DeleteElement, ShowContextMenu}
import scalafx.application.Platform
import com.auginte.desktop.zooming.Grid
import language.implicitConversions

/**
 * Actor for asynchronous actions with View object
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class View extends Actor {
  private var view: Option[desktop.View] = None
  private var grid: Option[Grid] = None

  private def v: desktop.View = view match {
    case Some(viewObject) => viewObject
    case None => throw new IllegalArgumentException("Using View actor without View model sent to actor")
  }

  def receive = {
    case v: desktop.View => {
      registerView(v)
      view = Some(v)
    }
    case g: Grid => {
      grid = Some(g)
    }
    case _ if view.isEmpty => throw new IllegalArgumentException("Received, but view is NOT initialised")
    case ShowContextMenu(source) => {
      Platform.runLater {
        v.contextMenu.show()
      }
      Platform.runLater {
        v.contextMenu.showContent(source.operations)
      }
    }
    case _ if grid.isEmpty => throw new IllegalArgumentException("Received, but grid is NOT initialised")
    case InsertElement(element, x, y) => {
      registerView(element)
      Platform.runLater {
        element.setTranslateX(x)
        element.setTranslateY(y)
        v.content.add(element)
      }
    }
    case DeleteElement(element) => {
      Platform.runLater {
        v.remove(element)
      }
    }
    case MoveElement(element, diffX, diffY) => {
      Platform.runLater {
        element.setTranslateX(element.getTranslateX + diffX)
        element.setTranslateY(element.getTranslateY + diffY)
      }
    }
    case _ => Unit
  }

  private def registerView(element: Any) = element match {
    case v: ViewableNode => synchronized(v.setView(this))
    case _ => Unit
  }


  //
  // Helpers
  //

  private implicit def AnyToRunnable(f: => Any): Runnable = new Runnable {
    override def run(): Unit = f
  }
}
