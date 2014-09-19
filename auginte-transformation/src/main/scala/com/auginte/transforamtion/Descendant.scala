package com.auginte.transforamtion

/**
 * Element, that can have one or more sources specified.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Descendant {
  private var _sources: List[Relation] = List()

  def sources: List[Relation] = _sources

  def sources_=(values: List[Relation]): Unit = _sources = values
}
