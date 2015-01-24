package com.auginte.desktop.persistable

import com.auginte.common.Unexpected

import scalafx.scene.input.{KeyEvent, MouseEvent}
import com.auginte.desktop.rich.RichJPane
import com.auginte.distribution.orientdb.{ReferWrapper, ReferConnection, RepresentationWrapper}
import scala.language.reflectiveCalls

/**
 * Initiating rendering of Refer connections on mouse over.
 */
trait MouseRenderingConnections extends RichJPane
with RepresentationWrapper
with ViewWrapper {
  private var connectionBeingRendered = false

  mouseEntered += ((e: MouseEvent) => if (needRenderingReferConnections(e)) renderAllReferConnections())

  mouseMoved += ((e: MouseEvent) => if (needRenderingReferConnections(e)) renderAllReferConnections())

  //TODO: delegate key events to children
  keyPressed += ((e: KeyEvent) => if (needRenderingReferConnections(e)) renderAllReferConnections())

  mouseExited += ((e: MouseEvent) => hideAllReferConnections())

  mouseDragged += ((e: MouseEvent) => hideAllReferConnections())

  //TODO: update on view zoom
  scrolled += refreshConnectionPositions


  private def refreshConnectionPositions(e: HotKeyEvent): Unit = {
    hideAllReferConnections()
    if (needRenderingReferConnections(e)) {
      renderAllReferConnections()
    }
  }

  private def renderAllReferConnections(): Unit = inView { v =>
    for (source <- distantSourceRepresentations()) source match {
      case VisualReferConnection(to, from, distance) =>
        v.renderConnection(center(from), center(to), opacity(distance))
      case _ => Unit
    }
    for (source <- distantDerivedRepresentations()) source match {
      case VisualReferConnection(from, to, distance) =>
        v.renderConnection(center(from), center(to), opacity(distance) / 3)
      case _ => Unit
    }
    connectionBeingRendered = true
  }

  private def hideAllReferConnections(): Unit = inView { v =>
    v.hideConnections()
    connectionBeingRendered = false
  }

  private def inView(f: RenderingConnections => Any): Unit = view match {
    case Some(v: RenderingConnections) => f(v)
    case other => Unexpected.state(s"Cannot use Refer rendering, as view is $other")
  }

  private def filerNodes(elements: Iterable[_]): Iterable[RichJPane] = elements.flatMap {
    case e: RichJPane => Some(e)
    case _ => None
  }

  private def opacity(distance: Double, max: Double = ReferWrapper.defaultDepth) = inBounds((max - distance) / max, 1)

  private def inBounds(value: Double, max: Double) = if (value < max) value else max

  private def center(node: RichJPane) = (node.getLayoutX + (node.getWidth / 2), node.getLayoutY + (node.getHeight / 2))


  protected def needRenderingReferConnections(e: HotKeyEvent): Boolean = e.shiftDown && !connectionBeingRendered
}
