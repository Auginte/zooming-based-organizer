package com.auginte.distribution.orientdb

import java.{lang => jl}

import com.orientechnologies.orient.core.db.record.ORecordLazySet
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
    val links = document.field[ORidBag](field)
    if (links == null || links.isEmpty) None
    else {
      val iterator = links.rawIterator()
      if (iterator.hasNext) Some(iterator.next().getIdentity)
      else None
    }
  } catch {
    case e: Exception =>
      Console.err.println("CommonSql edge EXCEPTION: " + e.getMessage)
      None
  }

  def edges(document: ODocument, field: String): Iterable[ORID] = {
    val links = document.field[ORidBag](field)
    if (links == null || links.isEmpty) EmptyDocumentIterable.map(_.getIdentity)
    else orientToScalaIterator(links)
  }

  private def orientToScalaIterator(iterable: ORidBag): Iterable[ORID] = {
    val iterator = iterable.rawIterator()
    var ids = List[ORID]()
    while (iterator.hasNext) {
      ids = iterator.next().getIdentity :: ids
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
