package com.auginte.desktop.actors

import com.auginte.desktop
import akka.actor.{ActorRef, Props, Actor}

/**
 * Actor to manage data integrity over many Views.
 *
 * Creates child actors for each new GUI View object.
 *
 * @see [[com.auginte.desktop.actors.View]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class Views extends Actor {
  override def receive = {
    case view: desktop.View => {
      val child = context.actorOf(Props[View], s"view${context.children.size}")
      child ! view
    }
    case _ => Unit
  }
}
