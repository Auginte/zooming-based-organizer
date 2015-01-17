package com.auginte.distribution.orientdb

import com.auginte.common.Unexpected
import com.auginte.distribution.orientdb.{Representation => R}
import com.tinkerpop.blueprints.impls.orient.OrientVertex

/**
 * Element, that have logical reference to other data (e.g. source or derives).
 */
trait ReferWrapper[A, Self <: ReferWrapper[A, Self]] { self: Self =>

  def storage: Persistable[A]

  def cloned: Self

  def copyLinked(swap: Boolean = false): Self = storage.persisted match {
    case Some(sourceData) =>
      val duplicated = cloned
      val targetData = duplicatedRecord(sourceData)
      if (swap) saveReference(targetData, sourceData) else saveReference(sourceData, targetData)
      List(sourceData, targetData).foreach(_.save())
      duplicated.storage.persisted = targetData
      duplicated
    case _ => Unexpected.state(s"Duplicating not persisted element: $this")
  }

  private def saveReference(source: OrientVertex, target: OrientVertex): Unit = reloadAnd(source, target) {
      target.addEdge("Refer", source)
      target.save()
    }

  def sourceRepresentations(implicit cache: R.Cached = R.defaultCache): Iterable[RepresentationWrapper] =
    storage.persisted match {
      case None => EmptyRepresentationStorageIterable
      case Some(persisted) => CommonSql.edges(persisted.getRecord, "in_Refer") flatMap (cache(_))
    }

  def derivedRepresentations(implicit cache: R.Cached = R.defaultCache): Iterable[RepresentationWrapper] =
    storage.persisted match {
      case None => EmptyRepresentationStorageIterable
      case Some(persisted) => CommonSql.edges(persisted.getRecord, "out_Refer") flatMap (cache(_))
    }

}
