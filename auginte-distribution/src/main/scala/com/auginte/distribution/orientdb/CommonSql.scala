package com.auginte.distribution.orientdb

import java.{lang => jl}

import com.orientechnologies.orient.core.db.record.OIdentifiable
import com.orientechnologies.orient.core.db.record.ridbag.ORidBag
import com.orientechnologies.orient.core.exception.OConcurrentModificationException
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientBaseGraph}

object CommonSql {
  def selectVertex(db: OrientBaseGraph)(sql: String) =
    db.command(new OCommandSQL(sql)).execute[jl.Iterable[OrientVertex]]()

  def edge(document: ODocument, field: String): Option[ODocument] = {
    val links = document.field[ORidBag](field)
    if (links == null || links.isEmpty) None
    else Some(links.iterator().next().getRecord[ODocument])
  }

  def edges(document: ODocument, field: String): Iterable[ODocument] = {
    val links = document.field[ORidBag](field)
    if (links == null || links.isEmpty) EmptyDocumentIterable
    else proxyIterable[OIdentifiable, ODocument](links, _.getRecord[ODocument])
  }

  def select(sql: String) = new OSQLSynchQuery[ODocument](sql.replace("\n", " ").trim)
}
