package com.auginte.desktop.events

import com.auginte.desktop.HaveOperations

/**
 * Event for context menu to show.
 *
 * {{{
 *   // val this = new Label("Test")
 *   val view: ActorRef = akka.actorOf(Props[act.View], "view1")
 *   view ! ShowContextMenu(this)
 * }}}
 *
 * @see [[com.auginte.desktop.operations.ContextMenu]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class ShowContextMenu(source: HaveOperations)
