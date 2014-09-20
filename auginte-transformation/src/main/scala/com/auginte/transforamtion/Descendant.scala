package com.auginte.transforamtion

import com.auginte.common.WithId

/**
 * Element, that can have one or more sources specified.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait Descendant extends WithId {
  private var _sources: List[Relation] = List()

  def sources: List[Relation] = _sources

  def sources_=(values: List[Relation]): Unit = _sources = values
}
