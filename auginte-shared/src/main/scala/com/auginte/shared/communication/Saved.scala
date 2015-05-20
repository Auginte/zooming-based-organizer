package com.auginte.shared.communication

import com.auginte.shared.state.persistable.Storage

/**
 * Wrapper for backend response
 */
case class Saved(success: Boolean, id: String, hash: String) {
  def storage = Storage(id, hash)
}
