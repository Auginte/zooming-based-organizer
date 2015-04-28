package com.auginte.scalajs.state.persistable

/**
 * Data structure representing independent data element
 */
case class Element(id: Int, text: String, x: Double = 0, y: Double = 0, width: Double = 10, height: Double = 10)