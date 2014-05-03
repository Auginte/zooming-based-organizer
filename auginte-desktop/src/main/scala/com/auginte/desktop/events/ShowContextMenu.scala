package com.auginte.desktop.events

import scalafx.scene.{input => sfxi}
import javafx.scene.{input => jfxi}
import javafx.scene.{control => jfxc}
import javafx.{scene => jfxs}
import com.auginte.desktop.rich.RichNode
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
 * @see [[com.auginte.desktop.ContextMenu]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class ShowContextMenu(source: HaveOperations)
