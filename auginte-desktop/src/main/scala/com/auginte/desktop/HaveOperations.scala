package com.auginte.desktop

import scalafx.event.ActionEvent

/**
 * Interface for ability to provide object specific operations
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait HaveOperations {
  type Operations = Map[String, ActionEvent => Any]

  def operations: Operations
}
