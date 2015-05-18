package com.auginte.server.storage

import com.auginte.shared.state.persistable.{Storage, Element, Camera, Persistable}
import com.orientechnologies.orient.core.metadata.schema.{OClass, OSchema, OType}
import com.tinkerpop.blueprints.impls.orient.{OrientVertex, OrientGraphNoTx}
import org.joda.time.{DateTimeZone, DateTime}
import play.api.{Configuration, Play}

object DatabaseStorage {
  def connection(config: Configuration): Option[OrientGraphNoTx] = {
    for (
      dbPath <- config.getString("orientdb.path");
      dbName <- config.getString("orientdb.db.main");
      dbUser <- config.getString("orientdb.user");
      dbPassword <- config.getString("orientdb.password")
    ) yield new OrientGraphNoTx(s"$dbPath/$dbName", dbUser, dbPassword)
  }

  def storeToDb(db: OrientGraphNoTx, persistable: Persistable) = {
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

  private def prepareStructure(db: OrientGraphNoTx): Unit = {
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

  private def storeCamera(db: OrientGraphNoTx, camera: Camera): OrientVertex = db.addVertex(
    "class:Camera",
    "x", Double.box(camera.x),
    "y", Double.box(camera.y),
    "scale", Double.box(camera.scale),
    "created", now
  )

  private def storeElement(db: OrientGraphNoTx, element: Element): OrientVertex = db.addVertex(
    "class:Element",
    "key", Int.box(element.id),
    "text", element.text,
    "x", Double.box(element.x),
    "y", Double.box(element.y),
    "width", Double.box(element.width),
    "height", Double.box(element.height),
    "created", now
  )

  private def storeUser(db: OrientGraphNoTx, storage: Storage): OrientVertex = db.addVertex(
    "class:User",
    "hash", storage.hash,
    "created", now
  )

  private def storeNode(db: OrientGraphNoTx, x: Int = 0, y: Int = 0): OrientVertex = db.addVertex(
    "class:Node",
    "x", Int.box(x),
    "y", Int.box(y)
  )

  private def now = new DateTime(DateTimeZone.UTC).toString()
}
