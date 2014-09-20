package com.auginte.transforamtion

/**
 * Elements that can be cloned, by keeping reference to source
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Transformable[A <: Transformable[A]] extends Descendant {
  // self-typing for copy to return this type
  self: A =>

  /**
   * Creates copy of object with relation to source.
   *
   * @param parameters context of relation. E.g. type of relation
   * @return copy, keeping relation to source
   */
  def transformed(parameters: Map[String, String] = Map()): Transformable[A] = transformedTo(copy, parameters)

  /**
   * Appends source with context to new object
   *
   * @param target new element to append reference
   * @param parameters context of relation. E.g. type of relation
   * @return copy, keeping relation to source
   */
  def transformedTo[B <: Transformable[B]](target: Transformable[B], parameters: Map[String, String] = Map()): Transformable[B] = {
    target.sources = List(Relation(this, parameters)) ++ target.sources
    target
  }

  /**
   * Create independent copy of element
   *
   * @return Deep cloned object
   */
  protected def copy: Transformable[A]
  
  def encapsualted = this
}
