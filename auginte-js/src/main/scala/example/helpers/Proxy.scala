package example.helpers

/**
 *
 */
/**
 * Callback data structure.
 *
 * @tparam A Immutable data for child (parameters)
 * @tparam B Immutable data structure of parent class (context)
 * @tparam C Logic class for callbacks
 */
trait Proxy[A, B, C] {
  def receive(data: A)(implicit sender: B = element): Unit

  def element: B

  def camera: C
}
