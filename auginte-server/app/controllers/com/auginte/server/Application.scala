package controllers.com.auginte.server

import com.auginte.shared.state.persistable.{Element, Camera, Persistable}
import com.orientechnologies.orient.core.metadata.schema.{OType, OClass, OSchema}
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx
import org.joda.time.{DateTimeZone, DateTime}
import play.api.mvc._
import prickle._
import play.api.Play
import play.api.Play.current

import scala.util.{Failure, Success}

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Testas"))
  }

  def store = Action { request =>
    request.body.asFormUrlEncoded.flatMap(m => m.get("data").flatMap(m => m.headOption)) match {
      case Some(json: String) => Unpickle[Persistable].fromString(json) match {
        case Success(persitable) =>
          storeToDb(persitable)
        case Failure(error) => BadRequest("Malformed data: " + error.getMessage)
      }
      case None => BadRequest("No data sent")
    }
  }

  private def storeToDb(persistable: Persistable) = {
    connectToDb() match {
      case Some(db) =>
        prepareStructure(db)
        storeCamera(db, persistable.camera)
        persistable.container.elements.values.foreach(storeElement(db, _))
        Ok("Saved")
      case None =>
        InternalServerError("Cannot connect to database")
    }
  }

  private def connectToDb(): Option[OrientGraphNoTx] = {
    val config = Play.application.configuration
    for (
      dbPath <- config.getString("orientdb.path");
      dbName <- config.getString("orientdb.db.main");
      dbUser <- config.getString("orientdb.user");
      dbPassword <- config.getString("orientdb.password")
    ) yield new OrientGraphNoTx(s"$dbPath/$dbName", dbUser, dbPassword)
  }

  private def prepareStructure(db: OrientGraphNoTx): Unit = {
    val schema = db.getRawGraph.getMetadata.getSchema
    val vertex = schema.getClass("V")
    val edge = schema.getClass("E")
    val camera = createClass(schema, "Camera", vertex)
    val element = createClass(schema, "Element", vertex)

    if (camera.getCustom("Version") == "" && element.getCustom("Version") == "") {
      List("x", "y", "scale").foreach(camera.createProperty(_, OType.DOUBLE))
      element.createProperty("key", OType.INTEGER)
      element.createProperty("text", OType.STRING)
      List("x", "y", "width", "height").foreach(element.createProperty(_, OType.DOUBLE))
      List(camera, element).foreach(_.setCustom("Version", "0.8.1"))
      List(camera, element).foreach(_.createProperty("created", OType.DATETIME))

    }
  }

  private def createClass(schema: OSchema, name: String, parent: OClass) =
    if (schema.existsClass(name)) schema.getClass(name).setSuperClass(parent)
    else schema.createClass(name, parent)

  private def storeCamera(db: OrientGraphNoTx, camera: Camera): Unit = {
    db.addVertex(
      "class:Camera",
      "x", Double.box(camera.x),
      "y", Double.box(camera.y),
      "scale", Double.box(camera.scale),
      "created", now
    )
  }

  private def storeElement(db: OrientGraphNoTx, element: Element): Unit = {
    db.addVertex(
      "class:Element",
      "key", Int.box(element.id),
      "text", element.text,
      "x", Double.box(element.x),
      "y", Double.box(element.y),
      "width", Double.box(element.width),
      "height", Double.box(element.height),
      "created", now
    )
  }

  private def now = new DateTime(DateTimeZone.UTC).toString()
}
