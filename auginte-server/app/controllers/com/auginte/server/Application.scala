package controllers.com.auginte.server

import com.auginte.server.storage.DatabaseStorage
import com.auginte.shared.state.persistable.Persistable
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
          val config = Play.application.configuration
          DatabaseStorage.connection(config) match {
            case Some(db) =>
              val user = DatabaseStorage.storeToDb(db, persitable)
              Ok(s"Saved: ${user.getId}")
            case None =>
              InternalServerError("Cannot connect to database")
          }
        case Failure(error) => BadRequest("Malformed data: " + error.getMessage)
      }
      case None => BadRequest("No data sent")
    }
  }
}
