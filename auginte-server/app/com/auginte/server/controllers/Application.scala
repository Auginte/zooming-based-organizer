package com.auginte.server.controllers

import com.auginte.server.storage.DatabaseStorage
import com.auginte.shared.communication.Saved
import com.auginte.shared.state.persistable.{Storage, Persistable}
import play.api.mvc._
import prickle._
import play.api.Play
import play.api.Play.current

import scala.util.{Failure, Success}

object Application extends Controller {
  type DB = DatabaseStorage.DB

  def index = Action {
    showNew(DatabaseStorage.newHash)
  }

  def load(id: String, hash: String) = Action {
    withDB { db =>
      DatabaseStorage.laodUser(db, s"#$id", hash) match {
        case Some(user) =>
          showLoaded(DatabaseStorage.generatePersistable(user))
        case None =>
          showNew(hash)
      }
    }
  }

  private def showNew(hash: String) = Ok(com.auginte.server.views.html.index(hash))

  private def showLoaded(persistable: Persistable) = {
    val serialised = Pickle.intoString(persistable)
    val storage = persistable.storage
    Ok(com.auginte.server.views.html.index(storage.hash, storage.id, serialised))
  }

  def store(hash: String) = Action { request =>
    request.body.asFormUrlEncoded.flatMap(m => m.get("data").flatMap(m => m.headOption)) match {
      case Some(json: String) => Unpickle[Persistable].fromString(json) match {
        case Success(persitable) =>
          try {
            withDB { db =>
              val user = DatabaseStorage.storeToDb(db, persitable.withStorage(new Storage("", hash)))
              val id = user.getId.toString.substring(1)
              val response = Pickle.intoString(Saved(success = true, id, hash))
              Ok(response)
            }
          } catch {
            case e: Exception =>
              //FIXME: debugging
              val config = Play.application.configuration
              println("FAILED TO CONNECT WITH:")
              println(config.getString("orientdb.path"))
              println(config.getString("orientdb.db.main"))
              println(config.getString("orientdb.user"))
              println(config.getString("orientdb.password"))
              InternalServerError("Cannot connect to database")
          }
        case Failure(error) => BadRequest("Malformed data: " + error.getMessage)
      }
      case None => BadRequest("No data sent")
    }
  }

  private def withDB[A](online: DB => A)(implicit failure: A = InternalServerError("Cannot connect to database")): A = {
    val config = Play.application.configuration
    DatabaseStorage.connection(config) match {
      case Some(db) => online(db)
      case None => failure
    }
  }
}
