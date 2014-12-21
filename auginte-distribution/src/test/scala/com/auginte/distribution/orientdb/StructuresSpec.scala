package com.auginte.distribution.orientdb

import java.{lang => jl}

import com.auginte.test.UnitSpec
import com.orientechnologies.orient.core.command.script.OCommandScript
import com.orientechnologies.orient.core.db.record.ridbag.ORidBag
import com.orientechnologies.orient.core.exception.OValidationException
import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.metadata.schema.OType
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery
import com.tinkerpop.blueprints.{Direction, Vertex}
import com.tinkerpop.blueprints.impls.orient.{OrientGraphNoTx, OrientBaseGraph}

import collection.JavaConversions._
import scala.language.implicitConversions
import TestDbHelpers._

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
      "suggest tree structure of Nodes" in {
        val db = Structure.createRepository(testDbName, "memory")
        val n0 = db.addVertex("class:Node", "x", Byte.box(0), "y", Byte.box(0))
        val n1 = db.addVertex("class:Node", "x", Byte.box(1), "y", Byte.box(2))
        val n2 = db.addVertex("class:Node", "x", Byte.box(3), "y", Byte.box(4))
        val e1 = n1.addEdge("Parent", n0)
        val e2 = n2.addEdge("Parent", n0)
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
            assert(Set(id0) === v.field[ORidBag]("out_Parent").toIterator.map(_.getIdentity).toSet)
          case id if id == id2 =>
            assert(3 === v.field[Byte]("x"))
            assert(4 === v.field[Byte]("y"))
            assert(null == v.field[ORidBag]("in_Parent"))
            assert(Set(id0) === v.field[ORidBag]("out_Parent").toIterator.map(_.getIdentity).toSet)
          case other =>
            fail("Unexpected row:" + dump[String](v))
        }
      }
      "create Representation vertex and Inside edge" in {
        val db = Structure.createRepository(testDbName, "memory")
        assert(true === schema(db).existsClass("Representation"))
        assert(true === schema(db).existsClass("Inside"))
      }
      "ensure Representation(x,y,scale) fields are mandatory" in {
        val db = Structure.createRepository(testDbName, "memory")
        val valid = db.addVertex("class:Representation", "x", double(1.1), "y", double(2.2), "scale", double(3.3))
        assert(1.1 === valid.getProperty[Double]("x"))
        assert(2.2 === valid.getProperty[Double]("y"))
        assert(3.3 === valid.getProperty[Double]("scale"))
        intercept[OValidationException] {
          db.addVertex("Representation", "Representation")
        }
        intercept[OValidationException] {
          db.addVertex("class:Representation", "x", Double.box(1.1))
        }
        intercept[OValidationException] {
          db.addVertex("class:Representation", "y", Double.box(2.2))
        }
        val after = db.addVertex("class:Representation", "x", double(-1.1), "y", double(-2.2), "scale", double(0.9))
        assert(-1.1 === after.getProperty[Double]("x"))
        assert(-2.2 === after.getProperty[Double]("y"))
        assert(0.9 === after.getProperty[Double]("scale"))
      }
      "suggest Representations can be attached to Nodes " in {
        val db = Structure.createRepository(testDbName, "memory")
        val node = db.addVertex("class:Node", "x", Byte.box(1), "y", Byte.box(2))
        val r1 = db.addVertex("class:Representation", "x", double(1.1), "y", double(2.2), "scale", double(3.3))
        val r2 = db.addVertex("class:Representation", "x", double(3), "y", double(-4), "scale", double(0.9))
        r1.addEdge("Inside", node)
        r2.addEdge("Inside", node)
      }
      "create Text representation with constrains" in {
        val db = Structure.createRepository(testDbName, "memory")
        assert(true === schema(db).existsClass("Text"))
        assert(classMeta(db, "Text").getProperty("x").isMandatory, "Parent class fields")
        assert(classMeta(db, "Text").getProperty("text").isMandatory)
      }
      "create Image representation constrains" in {
        val db = Structure.createRepository(testDbName, "memory")
        assert(true === schema(db).existsClass("Image"))
        assert(classMeta(db, "Image").getProperty("x").isMandatory, "Parent class fields")
        assert(classMeta(db, "Image").getProperty("path").isMandatory)
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
      "leave old Node and Parent data unchanged" in {
        val path = testDbName
        val connectionType = "memory"
        val db = new OrientGraphNoTx(s"$connectionType:$path")
        val n0 = db.addVertex("class:Node", "x", Byte.box(0))
        val n1 = db.addVertex("class:Node", "x", Byte.box(1))
        val e1 = n1.addEdge("Parent", n0)
        val db2 = Structure.createRepository(path, connectionType)
        assert(true === schema(db).existsClass("Node"))
        assert(true === schema(db).existsClass("Parent"))
        assert(2 === db.countVertices("Node"))
        for (v <- db.getVertices) {
          assert(1 === v.getEdges(Direction.BOTH, "Parent").toIterator.size)
          assert(!v.getPropertyKeys.contains("y"))
        }
      }
      "leave old Representation and Inside data unchanged" in {
        val path = testDbName
        val connectionType = "memory"
        val db = new OrientGraphNoTx(s"$connectionType:$path")
        val n0 = db.addVertex("class:Node", "x", Byte.box(0))
        val r1 = db.addVertex("class:Representation", "x", Byte.box(1))
        val e1 = r1.addEdge("Inside", n0)
        val db2 = Structure.createRepository(path, connectionType)
        assert(true === schema(db).existsClass("Node"))
        assert(true === schema(db).existsClass("Representation"))
        assert(true === schema(db).existsClass("Inside"))
        assert(1 === db.countVertices("Node"))
        assert(1 === db.countVertices("Representation"))
        for (v <- db.getVertices) {
          assert(1 === v.getEdges(Direction.BOTH, "Inside").toIterator.size)
          assert(!v.getPropertyKeys.contains("y"))
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

  def double(d: Double) = Double.box(d)
}
