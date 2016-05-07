package com.auginte.distribution.orientdb

import java.{lang => jl}

import com.orientechnologies.orient.core.db.record.{ORecordLazySet, OIdentifiable}
import com.orientechnologies.orient.core.db.record.ridbag.ORidBag
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientBaseGraph}

object CommonSql {
  def selectVertex(db: OrientBaseGraph)(sql: String) =
    db.command(new OCommandSQL(sql)).execute[jl.Iterable[OrientVertex]]()

  def edge(document: ODocument, field: String): Option[ODocument] = try {
    val links = document.field[ORidBag](field)
    if (links == null || links.isEmpty) None
    else {
      val edgeRecord = links.iterator().next().getRecord[ODocument]
      field match {
        case edgePattern(direction, edgeClass) if edgeRecord.getClassName == edgeClass =>
          Some(through1EdgeClass(direction, edgeRecord))
        case _ =>
          Some(edgeRecord)
      }
    }
  } catch {
    case e: Exception =>
      Console.err.println("CommonSql edge EXCEPTION: " + e.getMessage)
      None
  }

  def edges(document: ODocument, field: String): Iterable[ODocument] = {
    val links = document.field[ORidBag](field)
    if (links == null || links.isEmpty) EmptyDocumentIterable
    else {
      val edgeRecords = proxyIterable[OIdentifiable, ODocument](links, _.getRecord[ODocument])
      field match {
        case edgePattern(direction, edgeClass) =>
          for (record <- edgeRecords) yield {
            if (record.getClassName == edgeClass) record.field[ODocument](opposite(direction))
            else record
          }
        case _ => edgeRecords
      }
    }
  }

  private val edgePattern = "(in|out)_(.+)".r

  private def through1EdgeClass(direction: String, edgeRecord: ODocument) =
    edgeRecord.field[ORecordLazySet](opposite(direction)).iterator().next().asInstanceOf[ODocument]

  @inline
  private def opposite(direction: String) = if (direction == "in") "out" else "in"

  def select(sql: String) = new OSQLSynchQuery[ODocument](sql.replace("\n", " ").trim)
}
