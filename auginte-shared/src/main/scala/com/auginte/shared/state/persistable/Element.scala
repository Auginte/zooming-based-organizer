package com.auginte.shared.state.persistable

/**
 * Data structure representing independent data element
 */
case class Element(id: String, text: String, x: Double = 0, y: Double = 0, width: Double = 10, height: Double = 10, scale: Double = 1, selected: Boolean = false) {
  def moved(difference: Position) = copy(x = x + difference.x, y = y + difference.y)

  def withScale(newScale: Double) = copy(scale = newScale)
}