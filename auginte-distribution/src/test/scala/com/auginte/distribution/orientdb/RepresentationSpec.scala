package com.auginte.distribution.orientdb

import java.{lang => jl}

import com.auginte.distribution.orientdb.Representation.Creator
import com.auginte.test.UnitSpec
import com.orientechnologies.orient.core.db.record.ridbag.ORidBag
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.tinkerpop.blueprints.impls.orient.OrientVertex

import collection.JavaConversions._
import scala.language.implicitConversions
import TestDbHelpers._

/**
 * Unit test for [[com.auginte.distribution.orientdb.Representation]]
 *
 * Also covers representation subtypes:
 * * [[com.auginte.distribution.orientdb.Image]]
 * * [[com.auginte.distribution.orientdb.Text]]
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class RepresentationSpec extends UnitSpec {

  "OrientDB Representation storage" when {
    "storing to database" should {
      "save RID of newly created Representation record" in {
        val db = newDb
        val representation = Representation(1.1, 2.2, 3.3)
        assert(None === representation.persisted)
        val vertex = representation.storeTo(db)
        val vertices = db.getVertices.toList
        assert(1 === vertices.size)
        var fetchedRepresentation = vertices.head
        assert(1.1 === fetchedRepresentation.getProperty[Double]("x"))
        assert(2.2 === fetchedRepresentation.getProperty[Double]("y"))
        assert(3.3 === fetchedRepresentation.getProperty[Double]("scale"))
        assert(fetchedRepresentation.getId === representation.persisted.get.getIdentity)
        assert(vertex.getIdentity === representation.persisted.get.getIdentity)
      }
      "bind to loaded document (update without explicit save)" in {
        val db = newDb
        val representation = Representation(1.1, 2.2, 3.3)
        val crud = representation.storeTo(db)
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
      "store edge to Node" in {
        val db = newDb
        val node = new Node(1, 2, new Cache[Node])
        node.storeTo(db)
        val representation = new Representation(3, 4, 5)
        representation.storeTo(db)
        val nodes1 = select("SELECT x, in_Inside FROM Node")
        assert(null == nodes1.iterator().next().field[ORidBag]("in_Inside"))
        representation.node = node
        val nodes2 = select("SELECT x, in_Inside FROM Node")
        assert(1 === nodes2.iterator().next().field[ORidBag]("in_Inside").size())
        representation.node = node
        val nodes3 = select("SELECT x, in_Inside FROM Node")
        assert(1 === nodes3.iterator().next().field[ORidBag]("in_Inside").size())
        assert(1 === node.representations(representationCreator).size)
      }
    }
    "loading from database" should {
      "bind parameters from Orient Document results" in {
        val db = newDb
        val sql = execSql(db) _
        sql("CREATE VERTEX Representation set x=1.1, y=2.2, scale=3.3")
        sql("CREATE VERTEX Representation set x=4.4, y=5.5, scale=6.6")
        sql("CREATE VERTEX Representation set x=7.7, y=8.8, scale=9.9")

        val query = new OCommandSQL("SELECT FROM Representation WHERE scale > 4")
        val results = db.command(query).execute[jl.Iterable[OrientVertex]]()
        val representations = Representation.loadAll(results, representationCreator).toList.map(_.storage)
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
        val select = selectVertex(db) _
        val results = select("SELECT FROM Representation")
        val representations = Representation.loadAll(results, representationCreator).map(_.storage).toList

        val precision = floatToDoublePrecision
        assert(withPrecision(4.4, representations(0).x, precision))
        assert(withPrecision(0.0, representations(0).y, precision))
        assert(withPrecision(6.6, representations(0).scale, precision))
        assert(withPrecision(7.7, representations(1).x, precision))
        assert(withPrecision(8.8, representations(1).y, precision))
        assert(withPrecision(0.0, representations(1).scale, precision))

        representations(1).scale = 1.23
        val results2 = select("SELECT FROM Representation WHERE scale < 4")
        val representations2 = Representation.loadAll(results2, representationCreator).map(_.storage).toList
        assert(withPrecision(1.23, representations2(0).scale, precision))
      }
    }
    "using subtypes of representation" should {
      "store representations subtypes to database" in {
        val text = Text("hello")
        val image = Image("some.jpg")
        val r1 = Representation(1.1, 2.2, 3.3)
        val r2 = Representation(4.4, 5.5, 6.6)
        val r3 = Representation(7.7, 8.8, 9.9)
        text.representation = r2
        image.representation = r3
        val db = newDb
        for(representation <- List(r1, text, image)) {
          representation.storeTo(db)
        }
        val select = selectVertex(db) _
        val abstractRepresentations = select("SELECT FROM Representation WHERE x=1.1").toList
        val texts = select("SELECT FROM Representation WHERE x=4.4").toList
        val images = select("SELECT FROM Representation WHERE x=7.7").toList
        assert(1 === abstractRepresentations.size)
        assert(1 === texts.size)
        assert(1 === images.size)
        assert("hello" === texts.head.getProperty[String]("text"))
        assert("some.jpg" === images.head.getProperty[String]("path"))
      }
      "load representation subtypes by class type" in {
        val db = newDb
        val sql = execSql(db) _
        sql("CREATE VERTEX Representation, set x=1.1, y=2.2, scale=3.3")
        sql("CREATE VERTEX Image, set x=4.4, y=5.5, scale=6.6, path='some.jpg'")
        sql("CREATE VERTEX Text, set x=7.7, y=8.8, scale=9.9, text='hello'")
        val creator: Creator = {
          case "Text" => RepresentationWrapper(new Text())
          case "Image" => RepresentationWrapper(new Image())
          case _ => RepresentationWrapper(new Representation())
        }
        val representations = Representation.loadAll(db.getVertices, creator).toList
        assert(3 === representations.size)
        representations.map(_.storage).foreach {
          case Image(path, representation) =>
            assert("some.jpg" === path)
            assert(4.4 === representation.x)
            assert(5.5 === representation.y)
            assert(6.6 === representation.scale)
          case Text(text, representation) =>
            assert("hello" === text)
            assert(7.7 === representation.x)
            assert(8.8 === representation.y)
            assert(9.9 === representation.scale)
          case representation: Representation =>
            assert(1.1 === representation.x)
            assert(2.2 === representation.y)
            assert(3.3 === representation.scale)
          case unknown => fail(s"Unexpected class: $unknown")
        }
      }
    }
  }
}
