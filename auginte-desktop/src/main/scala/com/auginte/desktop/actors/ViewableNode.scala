package com.auginte.desktop.actors

import akka.actor.ActorRef
import com.auginte.desktop.{actors => a}

/**
 * Functionality to externally set and internally get view actor.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait ViewableNode {
  /**
   * There are no way to initialise placeholder Actor without creating Actor system,
   * which is memory and process intensive,
   */
  private var viewReference: Option[ActorRef] = None

  protected[actors] def setView(actorForView: a.View): Unit = {
    viewReference = Some(actorForView.self)
  }

  /**
   * Reference to [[com.auginte.desktop.actors.View]]
   *
   * @see [[com.auginte.desktop.actors.View]]
   * @throws  IllegalArgumentException if view is not initiated with [[setView]]
   */
  protected lazy val view: ActorRef = viewReference match {
    case Some(ref) => ref
    case None => throw new IllegalArgumentException("Using ViewableNode without setView")
  }
}
