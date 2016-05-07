package com.auginte.distribution.orientdb

import java.{lang => jl}

import com.orientechnologies.orient.core.db.record.{OIdentifiable, ORecordLazySet}
import com.orientechnologies.orient.core.db.record.ridbag.ORidBag
import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import com.tinkerpop.blueprints.impls.orient.{OrientBaseGraph, OrientVertex}

object CommonSql {
  def selectVertex(db: OrientBaseGraph)(sql: String) =
    db.command(new OCommandSQL(sql)).execute[jl.Iterable[OrientVertex]]()

  def edge(document: ODocument, field: String): Option[ORID] = try {
    def lightEdge(id: ORID) = Some(id)
    def intermediateTableEdge(links: ORidBag) = {
      val edgeRecord = links.iterator().next().getRecord[ODocument]
      field match {
        case edgePattern(direction, edgeClass) if edgeRecord.getClassName == edgeClass =>
          Some(through1EdgeClass(direction, edgeRecord).getIdentity)
        case _ =>
          Some(edgeRecord.getIdentity)
      }
    }

    val links = document.field[ORidBag](field)
    if (links == null || links.isEmpty) None
    else {
      val iterator = links.rawIterator()
      if (iterator.hasNext) {
        val id = iterator.next().getIdentity
        if (id.getClusterId == document.getIdentity.getClusterId) lightEdge(id)
        else intermediateTableEdge(links)
      }
      else None
    }
  } catch {
    case e: Exception =>
      Console.err.println("CommonSql edge EXCEPTION: " + e.getMessage)
      None
  }

  def edges(document: ODocument, field: String): Iterable[ORID] = {
    def toEdge(rec: OIdentifiable): ORID = rec match {
      case d: ODocument if d.fieldNames().toSet == Set("out", "in") =>
        val direction = opposite(field.split("_")(0))
        d.field[ODocument](direction).getIdentity
      case lightEdge => lightEdge.getIdentity
    }

    val links = document.field[ORidBag](field)
    if (links == null || links.isEmpty) EmptyORIDIterable
    else {
      val elements = orientToScalaIterator(links)
      elements.map(toEdge)
    }
  }

  private def orientToScalaIterator(iterable: ORidBag): List[OIdentifiable] = {
    val iterator = iterable.rawIterator()
    var ids = List[OIdentifiable]()
    while (iterator.hasNext) {
      ids = iterator.next():: ids
    }
    ids
  }

  private val edgePattern = "(in|out)_(.+)".r

  private def through1EdgeClass(direction: String, edgeRecord: ODocument) =
    edgeRecord.field[ORecordLazySet](opposite(direction)).iterator().next().asInstanceOf[ODocument]

  @inline
  private def opposite(direction: String) = if (direction == "in") "out" else "in"

  def select(sql: String) = new OSQLSynchQuery[ODocument](sql.replace("\n", " ").trim)
}
