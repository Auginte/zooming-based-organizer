package com.auginte.distribution.orientdb

import com.auginte.zooming.Grid
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientGraphNoTx, OrientBaseGraph}
import java.{lang => jl}
import collection.JavaConversions._

/**
 * Common function over multiple database tests
 */
object TestDbHelpers {
  type ODB = OrientBaseGraph

  def testDbName = s"test${System.currentTimeMillis()}"

  def newDb = Structure.createRepository(testDbName, "memory")

  def newEmptyDb = new OrientGraphNoTx(s"memory:$testDbName")

  def getFirst(db: ODB) = get(db).head

  def get(db: ODB) = db.getVertices.toList

  def execSql(db: ODB)(sql: String) = db.command(new OCommandSQL(sql)).execute[Unit]()

  def selectVertex(db: ODB)(sql: String) = db.command(new OCommandSQL(sql)).execute[jl.Iterable[OrientVertex]]()

  def select(sql: String) = new OSQLSynchQuery[ODocument](sql)

  def withPrecision(a: Double, b: Double, precision: Double) = (a - b).abs < precision

  val floatToDoublePrecision = 0.000001

  // Do not output OLogManager creation and shut down of database
  System.setProperty("log.console.level", "FINE")

  lazy val representationCreator: Representation.Creator = s => new Representation()

  def classMeta(db: ODB, className: String) = db.getRawGraph.getMetadata.getSchema.getClass(className)

  def newGrid = new Grid

  def newDbNode(db: OrientBaseGraph, x: Byte, y: Byte) =
    db.addVertex("class:Node", "x", Byte.box(x), "y", Byte.box(y))
}