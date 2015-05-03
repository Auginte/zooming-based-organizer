package com.auginte.scalajs.helpers

import com.auginte.scalajs.state.T
import japgolly.scalajs.react._

/**
 * Helper for logic implementation in React application
 *
 * @tparam S State class
 */
trait Backend[S] {
  val $: BackendScope[Unit, S]

  protected def m(converter: S => S) = $.modState(converter)

  def preventDefault(e: ReactEvent)(converter: S => S) = {
    if (!isInput(e.target.localName) && e.cancelable) {
      e.preventDefault()
    }
    m(converter)
  }

  def consume(e: ReactEvent)(converter: T) = {
    if (!isInput(e.target.localName) && e.cancelable) {
      e.preventDefault()
    }
  }

  private def isInput(tagName: String) = tagName.toLowerCase match {
    case "input" | "button" | "select" => true
    case _ => false
  }

  protected def preventAllDefault(e: ReactEvent)(converter: S => S) = {
    e.preventDefault()
    m(converter)
  }
}
