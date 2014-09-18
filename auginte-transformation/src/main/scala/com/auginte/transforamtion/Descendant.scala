package com.auginte.transforamtion

/**
 * Element, that can have one or more sources specified.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Descendant {
  private var _sources: Traversable[Relation] = List()

  def sources: Traversable[Relation] = _sources

  def sources_=(values: Traversable[Relation]): Unit = _sources = values
}
