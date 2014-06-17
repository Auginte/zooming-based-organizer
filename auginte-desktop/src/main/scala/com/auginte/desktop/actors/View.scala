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
import com.auginte.desktop.zooming.ZoomableElement

/**
 * Actor for asynchronous actions with View object.
 *
 * Takes care of Threads and concurrent modification issues.
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
            representation.contextMenu.showContent(source.operations)
          }
        }
        case InsertElement(element, x, y) => {
          registerView(element)
          element match {
            case e: ZoomableElement => representation.initializeInfinityZooming(e, x, y)
            case _ => translateNew(element, x, y)
          }
          Platform.runLater {
            representation.content.add(element)
          }
        }
        case DeleteElement(element) => Platform.runLater {
          representation.remove(element)
        }
        case MoveElement(element, node, diffX, diffY) => synchronized(representation.translate(node, diffX, diffY))
        case MoveView(camera, diffX, diffY) => synchronized(camera.translate(-diffX, -diffY))
        case ZoomView(camera, scale, x, y) => synchronized(representation.zoom(scale, x, y))
        case _ => Unit
      }
    }
  }

  private def registerView(element: Any) = element match {
    case v: ViewableNode => synchronized(v.setView(this))
    case _ => Unit
  }

  private def translateNew(element: jfxs.Node, x: Double, y: Double): Unit = Platform.runLater {
    element.setTranslateX(x)
    element.setTranslateY(y)
  }


  //
  // Helpers
  //

  private implicit def AnyToRunnable(f: => Any): Runnable = new Runnable {
    override def run(): Unit = f
  }
}
