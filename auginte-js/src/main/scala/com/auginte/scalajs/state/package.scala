package com.auginte.scalajs

import com.auginte.scalajs.events.logic.Event

/**
 * Data structures to store sate and do simple data manipulation
 */
package object state {
  import com.auginte.shared

  /**
   * Transforming GUI state object
   */
  type T = State => State

  type Actions = PartialFunction[Event, T]
}
