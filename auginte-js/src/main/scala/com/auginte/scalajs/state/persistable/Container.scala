package com.auginte.scalajs.state.persistable

import com.auginte.scalajs.state._

/**
 * Data structure to store Elements
 */
case class Container(elements: Map[Id, Element] = Map(), nextId: Int = 0) {
  def withNewElement(element: Element) = copy(elements = elements.updated(element.id, element), nextId = element.id + 1)

  def moved(elementId: Option[Id], difference: Position) = elementId match {
    case Some(id) if elements.contains(id) =>
      val original = elements(id)
      val element = original.copy(x = original.x + difference.x, y = original.y + difference.y)
      copy(elements = elements.updated(id, element))
    case _ => this
  }
}