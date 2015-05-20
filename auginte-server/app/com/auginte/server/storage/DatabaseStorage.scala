package com.auginte.server.storage

import com.auginte.shared.state.Id
import com.auginte.shared.state.persistable._
import com.auginte.shared.state.selected.Selectable
import com.orientechnologies.orient.core.command.traverse.OTraverse
import com.orientechnologies.orient.core.command.{OCommandContext, OCommandPredicate}
import com.orientechnologies.orient.core.id.ORecordId
import com.orientechnologies.orient.core.metadata.schema.{OClass, OSchema, OType}
import com.orientechnologies.orient.core.record.ORecord
import com.orientechnologies.orient.core.record.impl.ODocument
import com.tinkerpop.blueprints.Direction
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientGraphNoTx}
import org.joda.time.{DateTimeZone, DateTime}
import play.api.Configuration
import scala.util.Random
import scala.collection.JavaConversions._

object DatabaseStorage {
  type DB = OrientGraphNoTx
  type Vertex = OrientVertex

  def connection(config: Configuration): Option[DB] = {
    for (
      dbPath <- config.getString("orientdb.path");
      dbName <- config.getString("orientdb.db.main");
      dbUser <- config.getString("orientdb.user");
      dbPassword <- config.getString("orientdb.password")
    ) yield new DB(s"$dbPath/$dbName", dbUser, dbPassword)
  }

  def storeToDb(db: DB, persistable: Persistable) = {
    prepareStructure(db)
    val user = storeUser(db, persistable.storage)
    val camera = storeCamera(db, persistable.camera)
    val node = storeNode(db)
    val elements = persistable.container.elements.values.map(storeElement(db, _))
    user.addEdge("Owns", camera)
    camera.addEdge("View", node)
    elements.foreach(_.addEdge("Inside", node))
    user
  }

  private def prepareStructure(db: DB): Unit = {
    val schema = db.getRawGraph.getMetadata.getSchema
    val vertex = schema.getClass("V")
    val edge = schema.getClass("E")
    val newVertex = createClass(schema, _: String, vertex)
    val newEdge = createClass(schema, _: String, edge)
    val camera = newVertex("Camera")
    val element = newVertex("Element")
    val node = newVertex("Node")
    val user = newVertex("User")
    val owns = newEdge("Owns")
    val view = newEdge("View")

    if (camera.getCustom("Version") == "" && element.getCustom("Version") == "") {
      List("x", "y").foreach(node.createProperty(_, OType.INTEGER))
      List("x", "y", "scale").foreach(camera.createProperty(_, OType.DOUBLE))
      element.createProperty("key", OType.INTEGER)
      element.createProperty("text", OType.STRING)
      List("x", "y", "width", "height").foreach(element.createProperty(_, OType.DOUBLE))
      List("hash").foreach(user.createProperty(_, OType.STRING))
      List(camera, element, user).foreach(_.createProperty("created", OType.DATETIME))
      List(camera, element, node, user, owns, view).foreach(_.setCustom("Version", "0.8.2"))
    }
  }

  private def createClass(schema: OSchema, name: String, parent: OClass) =
    if (schema.existsClass(name)) schema.getClass(name).setSuperClass(parent)
    else schema.createClass(name, parent)

  private def storeCamera(db: DB, camera: Camera): Vertex = db.addVertex(
    "class:Camera",
    "x", Double.box(camera.x),
    "y", Double.box(camera.y),
    "scale", Double.box(camera.scale),
    "created", now
  )

  private def storeElement(db: DB, element: Element): Vertex = db.addVertex(
    "class:Element",
    "key", element.id,
    "text", element.text,
    "x", Double.box(element.x),
    "y", Double.box(element.y),
    "width", Double.box(element.width),
    "height", Double.box(element.height),
    "created", now
  )

  private def storeUser(db: DB, storage: Storage): Vertex = db.addVertex(
    "class:User",
    "hash", storage.hash,
    "created", now
  )

  private def storeNode(db: DB, x: Int = 0, y: Int = 0): Vertex = db.addVertex(
    "class:Node",
    "x", Int.box(x),
    "y", Int.box(y)
  )

  def newHash = Random.alphanumeric.take(64).mkString("")

  private def now = new DateTime(DateTimeZone.UTC).toString()

  def laodUser(db: DB, id: String, hash: String): Option[Vertex] = {
    val user = db.getVertex(id)
    if (user != null && user.getProperty[String]("hash") == hash) {
        Some(user)
    } else None
  }
  
  def generatePersistable(user: Vertex): Persistable = {
    val hash = user.getProperty[String]("hash")
    var camera = Camera()
    var elements: Map[Id, Element] = Map()
    for (element <- new OTraverse().fields("out_Owns", "out_View", "in_Inside", "out", "in").target(user).iterator()) {
      val record = element.getRecord[ODocument]
      record.getClassName match {
        case "Camera" => camera = Camera(record.field[Double]("x"), record.field[Double]("y"), record.field[Double]("scale"))
        case "Element" =>
          val converted = Element(
            record.getIdentity.toString,
            record.field[String]("text"),
            record.field[Double]("x"),
            record.field[Double]("y"),
            record.field[Double]("width"),
            record.field[Double]("height")
          )
          elements = elements + (converted.id -> converted)
        case _ => // Ignore User, Node and edges
      }
    }
    new Persistable(camera, Container(elements), Selectable(), Storage(user.getId.toString, hash))
  }
}
