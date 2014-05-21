package com.auginte.desktop.actors

import com.auginte.desktop
import akka.actor.Actor
import com.auginte.desktop.events._
import scalafx.application.Platform
import language.implicitConversions
import com.auginte.desktop.events.InsertElement
import com.auginte.desktop.events.ShowContextMenu
import com.auginte.desktop.events.DeleteElement
import com.auginte.desktop.events.MoveElement
import javafx.{scene => jfxs}
import scala.collection.JavaConversions._

/**
 * Actor for asynchronous actions with View object.
 *
 * Using hierarchy of supervisor-actor-representation:
   - [[com.auginte.desktop.actors.Views]] manages [[com.auginte.desktop.actors.View]],
     which manages his own [[com.auginte.desktop.View]].
   - [[com.auginte.desktop.View]] informs  [[com.auginte.desktop.actors.View]],
     which informs [[com.auginte.desktop.actors.Views]] and implements representation specific response.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class View extends Actor {
  private var viewOption: Option[desktop.View] = None

  def receive = {

    // Initialisation events
    case v: desktop.View => {
      registerView(v)
      viewOption = Some(v)
    }
    case _ if viewOption.isEmpty => throw new IllegalArgumentException("Received, but view is NOT initialised")

    // Events useful for all representations
    case event if (viewOption.isDefined) => {
      context.parent ! event
      val representation = viewOption.get

      // Representation specific implementation
      event match {
        case ShowContextMenu(source) => {
          Platform.runLater {
            representation.contextMenu.show()
          }
          Platform.runLater {
            representation.contextMenu.showContent(source.operations)
          }
        }
        case InsertElement(element, x, y) => {
          registerView(element)
          Platform.runLater {
            element.setTranslateX(x)
            element.setTranslateY(y)
            representation.content.add(element)
          }
        }
        case DeleteElement(element) => {
          Platform.runLater {
            representation.remove(element)
          }
        }
        case MoveElement(element, diffX, diffY) => {
          Platform.runLater {
            element.setTranslateX(element.getTranslateX + diffX)
            element.setTranslateY(element.getTranslateY + diffY)
          }
        }
        case MoveView(view, diffX, diffY) => {
          Platform.runLater {
            for (e <- view.getChildren) {
              e.setTranslateX(e.getTranslateX + diffX)
              e.setTranslateY(e.getTranslateY + diffY)
           }
          }
        }
        case _ => Unit
      }
    }
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
