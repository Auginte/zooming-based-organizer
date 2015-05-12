package com.auginte.scalajs.communication

import com.auginte.shared.state.persistable.Storage

/**
 * Wrapper for backend response
 */
case class Saved(success: Boolean, id: Int, hash: String) {
  def storage = Storage(id, hash)
}
