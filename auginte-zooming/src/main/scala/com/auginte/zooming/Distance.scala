package com.auginte.zooming

/**
 * Data structure to store absolute coordinates between 2 nodes
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
final case class Distance(x: Double = 0, y: Double = 0, scale: Double = 1) {
  def translated(diffX: Double, diffY: Double): Distance = Distance(x + diffX, y + diffY, scale)

  def zoomed(amount: Double): Distance = Distance(x, y, scale * amount)
}