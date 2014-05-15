package com.auginte.desktop.actors

import com.auginte.desktop
import akka.actor.Actor
import javafx.collections.ListChangeListener
import javafx.collections.ListChangeListener.Change
import javafx.scene.Node
import com.auginte.desktop.events.{MoveElement, DeleteElement, ShowContextMenu}
import scalafx.application.Platform

/**
 * Actor for asynchronous actions with View object
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class View extends Actor {
  private var view: Option[desktop.View] = None

  private def v: desktop.View = view match {
    case Some(viewObject) => viewObject
    case None => throw new IllegalArgumentException("Using View actor without View model sent to actor")
  }

  def receive = {
    case v: desktop.View => {
      initAddingListener(v)
      view = Some(v)
    }
    case _ if view.isEmpty => println("Received, but view NOT initialised")
    case ShowContextMenu(source) => {
      Platform.runLater {
        v.contextMenu.show()
      }
      Platform.runLater {
        v.contextMenu.showContent(source.operations)
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

  private def initAddingListener(view: desktop.View): Unit = {
    view.content.addListener(new ListChangeListener[Node] {
      override def onChanged(c: Change[_ <: Node]): Unit = while (c.next()) if (c.wasAdded()) {
        val element = view.content.get(c.getFrom)
        view.content.get(c.getFrom) match {
          case n: ViewableNode => {
            n.setView(View.this)
          }
          case _ => Unit
        }
      }
    })
  }
}
