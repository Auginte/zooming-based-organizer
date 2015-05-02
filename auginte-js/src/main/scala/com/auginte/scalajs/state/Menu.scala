package com.auginte.scalajs.state

/**
 * State identifying, if sidebar menu is expanded
 */
case class Menu(exapnded: Boolean = true) {
  def toggleExpanded = copy(exapnded = !exapnded)
}
