package example.communication

import example.state.Storage

/**
 * Wrapper for backend response
 */
case class Saved(success: Boolean, id: Int, hash: String) {
  def storage = Storage(id, hash)
}
