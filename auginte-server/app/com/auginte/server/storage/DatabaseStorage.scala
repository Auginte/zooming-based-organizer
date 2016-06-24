package com.auginte.server.storage

import com.auginte.shared.state.Id
import com.auginte.shared.state.persistable._
import com.auginte.shared.state.selected.Selectable
import com.orientechnologies.orient.client.remote.OServerAdmin
import com.orientechnologies.orient.core.command.traverse.OTraverse
import com.orientechnologies.orient.core.db.ODatabase
import com.orientechnologies.orient.core.metadata.schema.{OClass, OProperty, OSchema, OType}
import com.orientechnologies.orient.core.record.impl.ODocument
import com.tinkerpop.blueprints.impls.orient.{OrientGraphNoTx, OrientVertex}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Configuration

import scala.collection.JavaConversions._
import scala.util.{Failure, Random, Success, Try}

object DatabaseStorage {
  type DB = OrientGraphNoTx
  type Vertex = OrientVertex

  private val auginteVersionKey = "AuginteVersion"
  val javaDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

  private case class Credentials(path: String, name: String, user: String, password: String) {
    def toDb = new DB(s"$path/$name", user, password)

    def toServerAdmin: Try[OServerAdmin] = try {
      Success(new OServerAdmin(s"$path").connect(user, password))
    } catch {
      case e: Exception => Failure(e)
    }
  }

  def connection(config: Configuration): Option[DB] = readConfig(config).map(_.toDb)

  def provision(config: Configuration): (Boolean, String) = readConfig(config) match {
    case Some(credentials) => credentials.toServerAdmin match {
      case Success(connection) =>
        try {
          val databases =  connection.listDatabases()
          if (databases.containsKey(credentials.name)) {
            prepareStructure(credentials.toDb)
            (true, "Database updated")
          } else {
            connection.createDatabase(credentials.name, "graph", "plocal")
            prepareStructure(credentials.toDb)
            (true, "Database created and provisioned")
          }
        } catch {
          case e: Exception => (false, "Cannot create/update database: " + e.getMessage)
        }
      case Failure(e) => (false, "Bad database credentials: " + e.getMessage)
    }
    case None => (false, "Error reading database configuration")
  }

  private def readConfig(config: Configuration): Option[Credentials] = {
    for (
      dbPath <- config.getString("orientdb.path");
      dbName <- config.getString("orientdb.db.main");
      dbUser <- config.getString("orientdb.user");
      dbPassword <- config.getString("orientdb.password")
    ) yield Credentials(dbPath, dbName, dbUser, dbPassword)
  }

  def storeToDb(db: DB, persistable: Persistable) = {
    db.getRawGraph.set(ODatabase.ATTRIBUTES.DATETIMEFORMAT, javaDateFormat)
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

    db.getRawGraph.set(ODatabase.ATTRIBUTES.DATETIMEFORMAT, javaDateFormat)
    val current = Version(element.getCustom(auginteVersionKey), "0.8.2")
    if (current < Version("0.8.3")) {
      List("x", "y").foreach(createProperty(node, _, OType.INTEGER).setMandatory(true))
      List("x", "y", "scale").foreach(createProperty(camera, _, OType.DOUBLE).setMandatory(true))
      createProperty(element, "text", OType.STRING).setMandatory(true)
      List("x", "y", "width", "height").foreach(createProperty(element, _, OType.DOUBLE).setMandatory(true))
      List("hash").foreach(createProperty(user, _, OType.STRING).setMandatory(true))
      List(camera, element, user).foreach(createProperty(_, "created", OType.DATETIME).setMandatory(true))
    }
    if (current < Version("0.8.4")) {
      List("scale").foreach(createProperty(element, _, OType.DOUBLE))
    }
    List(camera, element, node, user, owns, view).foreach(_.setCustom(auginteVersionKey, "0.8.4"))
  }

  private def createClass(schema: OSchema, name: String, parent: OClass) =
    if (schema.existsClass(name)) schema.getClass(name).setSuperClasses(List(parent))
    else schema.createClass(name, parent)

  private def createProperty(table: OClass, name: String, rowType: OType): OProperty = {
    val property = table.getProperty(name)
    if (property == null) {
      table.createProperty(name, rowType)
    } else {
      property.setType(rowType)
    }
  }

  private def storeCamera(db: DB, camera: Camera): Vertex = db.addVertex(
    "class:Camera",
    "x", Double.box(camera.x),
    "y", Double.box(camera.y),
    "scale", Double.box(camera.scale),
    "created", now
  )

  private def storeElement(db: DB, element: Element): Vertex = {
    db.addVertex(
      "class:Element",
      "key", element.id,
      "text", element.text,
      "x", Double.box(element.x),
      "y", Double.box(element.y),
      "width", Double.box(element.width),
      "height", Double.box(element.height),
      "scale", Double.box(element.scale),
      "created", now
    )
  }

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

  def loadUser(db: DB, id: String, hash: String): Option[Vertex] = {
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
            record.field[Double]("height"),
            record.field[Double]("scale")
          )
          elements = elements + (converted.id -> converted)
        case _ => // Ignore User, Node and edges
      }
    }
    new Persistable(camera, Container(elements), Selectable(), Storage(user.getId.toString, hash))
  }
}
