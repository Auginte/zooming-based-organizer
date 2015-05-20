package com.auginte.shared.state.persistable

import com.auginte.shared.state.Id

/**
 * Data structure to store Elements
 */
case class Container(elements: Map[Id, Element] = Map(), nextId: Int = 0) {
  def withNewElement(element: Element) = copy(elements = elements.updated(element.id, element), nextId = nextId + 1)

  def moved(elementId: Option[Id], difference: Position) = elementId match {
    case Some(id) if elements.contains(id) =>
      val original = elements(id)
      val element = original.copy(x = original.x + difference.x, y = original.y + difference.y)
      copy(elements = elements.updated(id, element))
    case _ => this
  }
}