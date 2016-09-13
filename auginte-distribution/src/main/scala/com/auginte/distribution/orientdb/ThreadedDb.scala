package com.auginte.distribution.orientdb

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import com.tinkerpop.blueprints.impls.orient.{OrientBaseGraph, OrientVertex}

/**
  * Trying to fix usage of OrientDB `ThreadLocal[OrientBaseGraph] activeGraph` and `null` on `getGraph` methods
  */
object ThreadedDb {
  @volatile private var db: Option[OrientBaseGraph] = None
  @volatile private var rawDb: Option[ODatabaseDocumentTx] = None

  def getDefault = db

  def setDefault(db: OrientBaseGraph) = {
    this.db = Some(db)
    this.rawDb = Some(db.getRawGraph)
  }

  def activateOnThisThread() = rawDb match {
    case Some(connection) => connection.activateOnCurrentThread()
    case None => throw new AssertionError(s"Raw database not set")
  }

  def getGraph(vertex: OrientVertex): OrientBaseGraph = db match {
    case Some(graph) => graph
    case None =>
      val graph = vertex.getGraph
      if (graph == null) {
        throw new AssertionError(s"No orientDB in current Thread: ${Thread.currentThread()}")
      }
      graph
  }
}
