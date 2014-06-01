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
   * Returns copy with every parameter added
   */
  def +(d: Distance): Distance = Distance(x + d.x, y + d.y, scale + d.scale)

  /**
   * Returns copy with every parameter subtracted
   */
  def -(d: Distance): Distance = Distance(x - d.x, y - d.y, scale - d.scale)


  /**
   * Returns copy with every parameter multipied
   */
  def *(factor: Double): Distance = Distance(x * factor, y  * factor, scale * factor)


  /**
   * Returns copy with scale dependent sum.
   */
  def ++(d: Distance): Distance = {
    val factor = d.scale / scale
    Distance(x * factor + d.x, y * factor + d.y, scale)
  }

  /**
   * Returns copy with scale dependent difference.
   */
  def --(d: Distance): Distance = {
    val factor = d.scale / scale
    Distance(x * factor - d.x, y * factor - d.y, scale)
  }

  /**
   * Returns copy with position changed to match needed scale.
   *
   * For example:
   * {{{
   * scala> Distance(1.2, 3.4, 100) normalised 1
   * res1: com.auginte.zooming.Distance = Distance(120.0,340.0,1.0)
   *
   * scala> Distance(123, 45, 1) normalised 100
   * res2: com.auginte.zooming.Distance = Distance(1.23,0.45,100.0)
   * }}}
   */
  def normalised(scale: Double): Distance = {
    val factor = this.scale / scale
    Distance(x * factor, y * factor, scale)
  }
}