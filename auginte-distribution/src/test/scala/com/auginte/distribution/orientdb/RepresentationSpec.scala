package com.auginte.distribution.orientdb

import java.{lang => jl}

import com.auginte.test.UnitSpec
import com.orientechnologies.orient.core.command.script.OCommandScript
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal
import com.orientechnologies.orient.core.db.record.ridbag.ORidBag
import com.orientechnologies.orient.core.exception.OValidationException
import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass
import com.orientechnologies.orient.core.metadata.schema.OType
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import com.tinkerpop.blueprints.{Direction, Vertex}
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientGraphNoTx, OrientBaseGraph}

import collection.JavaConversions._
import scala.language.implicitConversions
import TestDbHelpers._


/**
 * Unit test for [[com.auginte.distribution.orientdb.Representation]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class RepresentationSpec extends UnitSpec {

  "OrientDB Representation storage" when {
    "storing to database" should {
      "save RID of newly created Representation record" in {
        val db = newDb
        var representation = new Representation(1.1, 2.2, 3.3)
        assert(None === representation.persisted)
        val vertex = Representation.store(representation, db)
        val vertices = db.getVertices.toList
        assert(1 === vertices.size)
        var fetchedRepresentation = vertices.head
        assert(1.1 === fetchedRepresentation.getProperty[Double]("x"))
        assert(2.2 === fetchedRepresentation.getProperty[Double]("y"))
        assert(3.3 === fetchedRepresentation.getProperty[Double]("scale"))
        assert(fetchedRepresentation.getId === representation.persisted.get.getIdentity)
        assert(vertex.getIdentity === representation.persisted.get.getIdentity)
      }
      "bind to loaded document (update wihout explicit save)" in {
        val db = newDb
        val representation = new Representation(1.1, 2.2, 3.3)
        val crud = Representation.store(representation, db)
        val fetched1 = getFirst(db)
        assert(1.1 === fetched1.getProperty[Double]("x"))
        assert(2.2 === fetched1.getProperty[Double]("y"))
        assert(3.3 === fetched1.getProperty[Double]("scale"))
        representation.x = 4.4
        representation.y = 5.5
        representation.scale = 6.6
        assert(4.4 == crud.getProperty[Double]("x"))
        assert(5.5 == crud.getProperty[Double]("y"))
        assert(6.6 == crud.getProperty[Double]("scale"))
        val fetched2 = getFirst(db)
        assert(4.4 == fetched2.getProperty[Double]("x"))
        assert(5.5 == fetched2.getProperty[Double]("y"))
        assert(6.6 == fetched2.getProperty[Double]("scale"))
      }
    }
    "loading from database" should {
      "bind parameters from Orient Document results" in {
        val db = newDb
        val sql = execSql(db) _
        sql("CREATE VERTEX Representation, set x=1.1, y=2.2, scale=3.3")
        sql("CREATE VERTEX Representation, set x=4.4, y=5.5, scale=6.6")
        sql("CREATE VERTEX Representation, set x=7.7, y=8.8, scale=9.9")

        val query = new OCommandSQL("SELECT FROM Representation WHERE scale > 4")
        val results = db.command(query).execute[jl.Iterable[OrientVertex]]()

        val representations = Representation.load(results).toList
        assert(2 === representations.size)
        assert(4.4 === representations(0).x)
        assert(5.5 === representations(0).y)
        assert(6.6 === representations(0).scale)
        assert(7.7 === representations(1).x)
        assert(8.8 === representations(1).y)
        assert(9.9 === representations(1).scale)
        representations(1).scale = 3.9999999999

        val results2 = db.command(query).execute[jl.Iterable[OrientVertex]]()
        assert(1 === results2.toList.size)
      }
      "use default values for unset fields, bind for saving" in {
        val db = newEmptyDb
        val sql = execSql(db) _
        sql("CREATE CLASS Representation EXTENDS V")
        sql("CREATE VERTEX Representation, set x=4.4, scale=6.6")
        sql("CREATE VERTEX Representation, set x=7.7, y=8.8")
        val query = new OCommandSQL("SELECT FROM Representation")
        val results = db.command(query).execute[jl.Iterable[OrientVertex]]()
        val representations = Representation.load(results).toList

        val precision = floatToDoublePrecision
        assert(withPrecision(4.4, representations(0).x, precision))
        assert(withPrecision(0.0, representations(0).y, precision))
        assert(withPrecision(6.6, representations(0).scale, precision))
        assert(withPrecision(7.7, representations(1).x, precision))
        assert(withPrecision(8.8, representations(1).y, precision))
        assert(withPrecision(0.0, representations(1).scale, precision))

        representations(1).scale = 1.23
        val query2 = new OCommandSQL("SELECT FROM Representation WHERE scale < 4")
        val results2 = db.command(query2).execute[jl.Iterable[OrientVertex]]()
        val representations2 = Representation.load(results2).toList
        assert(withPrecision(1.23, representations2(0).scale, precision))
      }
    }
  }
}
