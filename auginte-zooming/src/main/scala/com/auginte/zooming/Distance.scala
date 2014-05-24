package com.auginte.zooming

/**
 * Data structure to store absolute coordinates between 2 nodes
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
   * Returns copy with every parameter added
   */
  def +(d: Distance): Distance = Distance(x + d.x, y + d.y, scale + d.scale)

  /**
   * Returns copy with every parameter subtracted
   */
  def -(d: Distance): Distance = Distance(x - d.x, y - d.y, scale - d.scale)
}