package com.auginte.distribution.orientdb

import com.auginte.common.Unexpected
import com.auginte.distribution.orientdb.{Representation => R}
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.blueprints.impls.orient.OrientVertex
import collection.JavaConversions._
import java.{lang => jl}

/**
 * Element, that have logical reference to other data (e.g. source or derives).
 */
trait ReferWrapper[A, Self <: ReferWrapper[A, Self]] { self: Self =>

  import ReferWrapper._

  type SelfRepresentation = Self with RepresentationWrapper

  def storage: Persistable[A]

  def cloned: SelfRepresentation

  def copyLinked(swap: Boolean = false)(implicit cache: R.Cached = R.defaultCache): SelfRepresentation =
    storage.persisted match {
      case Some(sourceData) =>
        val duplicated = cloned
        val targetData = duplicatedRecord(sourceData)
        if (swap) saveReference(targetData, sourceData) else saveReference(sourceData, targetData)
        List(sourceData, targetData).foreach(_.save())
        duplicated.storage.persisted = targetData
        cache += targetData.getRecord -> duplicated
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

  def distantSourceRepresentations(maxDepth: Int = defaultDepth)(implicit cache: R.Cached = R.defaultCache): Iterable[ReferConnection] =
    distantRepresentations("out", maxDepth, cache)

  def distantDerivedRepresentations(maxDepth: Int = defaultDepth)(implicit cache: R.Cached = R.defaultCache): Iterable[ReferConnection] =
    distantRepresentations("in", maxDepth, cache)

  private def distantRepresentations(direction: String, maxDepth: Int, cache: R.Cached): Iterable[ReferConnection] =
    storage.persisted match {
      case Some(persisted) if persisted.getGraph != null =>
        val depth = "$depth"
        val sourceDocument: String = persisted.getIdentity.toString
        val sql =
          s"""
          |SELECT
          |   @rid AS edgeTo,
          |   traversedVertex(-2).@rid AS edgeFrom,
          |   $depth
          |FROM (
          |   TRAVERSE $direction('Refer')
          |   FROM $sourceDocument
          |   WHILE $depth < $maxDepth
          |)
          |WHERE @this instanceof 'Representation'
          |  AND $depth > 0
        """.stripMargin
        val query = new OCommandSQL(sql.replace("\n", " "))
        val parameters = JMap()
        persisted.getGraph.command(query).execute[Vertices](parameters).map { row =>
          val from = row.getProperty[ODocument]("edgeFrom")
          val to = row.getProperty[ODocument]("edgeTo")
          val distance = row.getProperty[Int]("$depth")
          cache(to) match {
            case Some(cachedTo) => cache(from) match {
              case Some(cacheFrom) => ReferConnection(cacheFrom, cachedTo, distance)
              case None => Unexpected.state(s"Distant representation out-edge not found in cache: $to in ${cache.get}")
            }
            case None => Unexpected.state(s"Distant representation in-edge not found in cache: $to in ${cache.get}")
          }
        }
      case _ => EmptyDistantRepresentationIterable
    }

  def derivedRepresentations(implicit cache: R.Cached = R.defaultCache): Iterable[RepresentationWrapper] =
    storage.persisted match {
      case None => EmptyRepresentationStorageIterable
      case Some(persisted) => CommonSql.edges(persisted.getRecord, "out_Refer") flatMap (cache(_))
    }
}

object ReferWrapper {
  val defaultDepth = 20

  type Vertices = jl.Iterable[Vertex]

  def JMap(arguments: Tuple2[String, Any]*) = mapAsJavaMap(Map(arguments: _*))
}