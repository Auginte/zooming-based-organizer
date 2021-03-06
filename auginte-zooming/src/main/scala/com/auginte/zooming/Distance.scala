package com.auginte.zooming

/**
 * Data structure to store absolute coordinates between 2 nodes
 *
 * Scale is indicating size: usually, larger scaling use larger positions.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
final case class Distance(x: Double = 0, y: Double = 0, scale: Double = 1) {
  /**
   * Returns copy with added x and y values.
   */
  def translated(diffX: Double, diffY: Double): Distance = Distance(x + diffX, y + diffY, scale)

  /**
   * Returns copy with multiplied scale
   */
  def zoomed(amount: Double): Distance = Distance(x, y, scale * amount)

  /**
   * Returns copy with provided scale value
   */
  def withScale(amount: Double): Distance = Distance(x, y, amount)

  /**
   * Inverts coordinates, so gui transformation is like camera's node change.
   * @return
   */
  def asCameraNode: Distance = Distance(-x, -y, 1 / scale)

  /**
   * Returns copy with every parameter added
   */
  def +(d: Distance): Distance = Distance(x + d.x, y + d.y, scale + d.scale)

  /**
   * Returns copy with every parameter subtracted
   */
  def -(d: Distance): Distance = Distance(x - d.x, y - d.y, scale - d.scale)


  /**
   * Returns copy with every parameter multiplied
   */
  def *(factor: Double): Distance = Distance(x * factor, y * factor, scale * factor)

  /**
   * Returns copy with every parameter divided
   */
  def /(factor: Double): Distance = Distance(x / factor, y / factor, scale / factor)

  /**
   * Returns copy with scale dependent difference.
   *
   * {{{
   *   D = (A_p / A_s  -  B_p / B_s,  A_s / B_s)
   *   // D - graphical difference
   *   // A, B - operands
   *   // p - position parameters (x or y)
   *   // s - scale parameter
   * }}}
   */
  def --(d: Distance): Distance = {
    Distance((x / scale) - (d.x / d.scale), (y / scale) - (d.y / d.scale), scale / d.scale)
  }

  override def clone(): Distance = copy(x, y, scale)
}