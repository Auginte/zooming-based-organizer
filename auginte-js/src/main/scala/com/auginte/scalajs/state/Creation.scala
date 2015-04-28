package com.auginte.scalajs.state

/**
 * Data structure for new elements box
 */
case class Creation(name: String = "") {
  def withName(newName: String) = copy(name = newName)

  def resetName = withName("")
}
