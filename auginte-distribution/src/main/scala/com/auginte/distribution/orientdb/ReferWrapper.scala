package com.auginte.distribution.orientdb

import com.auginte.common.Unexpected
import com.tinkerpop.blueprints.impls.orient.OrientVertex
import scala.collection.JavaConversions._

/**
 * Element, that have logical reference to other data (e.g. source).
 */
trait ReferWrapper[A, Self <: ReferWrapper[A, Self]] { self: Self =>

  def storage: Persistable[A]

  def cloned: Self

  def copyLinked: Self = storage.persisted match {
    case Some(sourceData) =>
      val duplicated = cloned
      val targetData = duplicatedRecord(sourceData)
      saveReference(sourceData, targetData)
      duplicated.storage.persisted = targetData
      duplicated
    case _ => Unexpected.state(s"Duplicating not persisted element: $this")
  }

  private def saveReference(source: OrientVertex, target: OrientVertex): Unit = reloadAnd(source){
      target.addEdge("Refer", source)
      target.save()
    }

}
