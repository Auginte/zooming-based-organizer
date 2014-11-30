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
import com.tinkerpop.blueprints.impls.orient.{OrientGraphNoTx, OrientBaseGraph}

import collection.JavaConversions._
import scala.language.implicitConversions

/**
 * Unit test for [[com.auginte.distribution.orientdb.Structure]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class StructuresSpec extends UnitSpec with StructuresSpecHelpers{

  "OrientDB Structures storage" when {
    "no database exists" should {
      "create Node vertex and Parent edge" in {
        val db = Structure.createRepository(testDbName, "memory")
        assert(true === schema(db).existsClass("Node"))
        assert(true === schema(db).existsClass("Parent"))
      }
      "ensure Node(x,y) fields are mandatory" in {
        val db = Structure.createRepository(testDbName, "memory")
        val valid = db.addVertex("class:Node", "x", Byte.box(1), "y", Byte.box(2))
        assert(1 === valid.getProperty[Byte]("x"))
        assert(2 === valid.getProperty[Byte]("y"))
        intercept[OValidationException] {
          db.addVertex("Node", "Node")
        }
        intercept[OValidationException] {
          db.addVertex("class:Node", "x", Byte.box(1))
        }
        intercept[OValidationException] {
          db.addVertex("class:Node", "y", Byte.box(2))
        }
        val afterException = db.addVertex("class:Node", "x", Int.box(-3), "y", Int.box(-4))
        assert(-3 === afterException.getProperty[Byte]("x"))
        assert(-4 === afterException.getProperty[Byte]("y"))
      }
      "ensure tree structure of Nodes" in {
        val db = Structure.createRepository(testDbName, "memory")
        val n0 = db.addVertex("class:Node", "x", Byte.box(0), "y", Byte.box(0))
        val n1 = db.addVertex("class:Node", "x", Byte.box(1), "y", Byte.box(2))
        val n2 = db.addVertex("class:Node", "x", Byte.box(3), "y", Byte.box(4))
        val e1 = n1.addEdge("Parent", n0)
        val e2 =n2.addEdge("Parent", n0)
        //      n0
        // e1  / \  e2
        //   n1   n2
        val (id0, id1, id2) = (n0.getIdentity, n1.getIdentity, n2.getIdentity)
        for (v <- executeSql(db)("select @rid, @class, x, y, in_Parent, out_Parent from Node")) rid(v) match {
          case id if id == id0 =>
            assert(0 === v.field[Byte]("x"))
            assert(0 === v.field[Byte]("y"))
            assert(Set(id1, id2) === v.field[ORidBag]("in_Parent").toIterator.toSet)
            assert(null == v.field[ORidBag]("out_Parent"))
          case id if id == id1 =>
            assert(1 === v.field[Byte]("x"))
            assert(2 === v.field[Byte]("y"))
            assert(null == v.field[ORidBag]("in_Parent"))
            assert(Set(id0) === v.field[ORidBag]("out_Parent").toIterator.toSet)
          case id if id == id2 =>
            assert(3 === v.field[Byte]("x"))
            assert(4 === v.field[Byte]("y"))
            assert(null == v.field[ORidBag]("in_Parent"))
            assert(Set(id0) === v.field[ORidBag]("out_Parent").toIterator.toSet)
          case other =>
            fail("Unexpected row:" + dump[String](v))
        }
      }
    }
    "old database exists" should {
      "add or update fields of Node and Parent" in {
        val path = testDbName
        val db = new OrientGraphNoTx(s"memory:$path")
        db.command(new OCommandScript("sql",
          """
            |create class Node;
            |create property Node.x Integer;
            |alter property Node.x mandatory false;
            |alter property Node.x readonly false;
          """.stripMargin)).execute()
        val nodeType = db.getRawGraph.getMetadata.getSchema.getClass("Node")
        assert(null == nodeType.getSuperClass)
        assert(OType.INTEGER === nodeType.getProperty("x").getType)
        assert(!nodeType.getProperty("x").isMandatory)
        assert(!nodeType.getProperty("x").isReadonly)
        val db2 = Structure.createRepository(path, "memory")
        assert("V" === db2.getVertexType("Node").getSuperClass.getName)
        assert(OType.BYTE === db2.getVertexType("Node").getProperty("x").getType)
        assert(db2.getVertexType("Node").getProperty("x").isMandatory)
        assert(db2.getVertexType("Node").getProperty("x").isReadonly)
      }
    }
    "up-to-date database exists" should {
      "leave Node and Parent structures unchanged" in {
        val path = testDbName
        val db = Structure.createRepository(path, "memory")
        val n0 = db.addVertex("class:Node", "x", Byte.box(0), "y", Byte.box(0))
        val n1 = db.addVertex("class:Node", "x", Byte.box(1), "y", Byte.box(2))
        val e1 = n1.addEdge("Parent", n0)
        val db2 = Structure.createRepository(path, "memory")
        assert(true === schema(db).existsClass("Node"))
        assert(true === schema(db).existsClass("Parent"))
        assert(2 === db.countVertices("Node"))
        for (v <- db.getVertices) {
          assert(1 === v.getEdges(Direction.BOTH, "Parent").toIterator.size)
        }
      }
    }
  }
}

trait StructuresSpecHelpers {
  def testDbName = s"test${System.currentTimeMillis()}"

  def schema(db: OrientBaseGraph) = db.getRawGraph.getMetadata.getSchema

  // Do not output OLogManager creation and shut down of database
  System.setProperty("log.console.level", "FINE")

  def executeSql(db: OrientBaseGraph)(sql: String) = new OSQLSynchQuery[ODocument](sql)

  def dump[A](v: Vertex) = v.getPropertyKeys.map(key => s"$key = ${v.getProperty[A](key)}").mkString("\t|\t")

  def dump[A](v: ODocument) = v.fieldNames().map(key => s"$key = ${v.field[A](key)}").mkString("\t|\t")

  private def OSynchQuery2Iterator(l :OSQLSynchQuery[ODocument]) = new Iterable[ODocument] {
    override def iterator = l.iterator()
  }

  def rid(row: ODocument) = row.field[ORID]("rid").getIdentity
}
