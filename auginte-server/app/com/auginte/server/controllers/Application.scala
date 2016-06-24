package com.auginte.server.controllers

import javax.inject.{Inject, Singleton}

import com.auginte.server.migration.PersistableMigration
import com.auginte.server.storage.DatabaseStorage
import com.auginte.shared.communication.Saved
import com.auginte.shared.state.persistable.{Persistable, Storage}
import play.api.mvc._
import play.api.{Configuration, Logger}
import prickle._

import scala.util.{Failure, Success}

@Singleton
class Application @Inject() (config: Configuration) extends Controller {
  type DB = DatabaseStorage.DB

  def index = Action {
    showNew(DatabaseStorage.newHash)
  }

  def load(id: String, hash: String) = Action {
    withDB { db =>
      DatabaseStorage.loadUser(db, s"#$id", hash) match {
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
      case Some(jsonData: String) => Unpickle[Persistable].fromString(jsonData) match {
        case Success(persistable) => storePersistable(persistable, hash)
        case Failure(_) =>
          val migrated = PersistableMigration.migrate(jsonData)
          Unpickle[Persistable].fromString(migrated) match {
            case Success(persistable) => storePersistable(persistable, hash)
            case Failure(error) => BadRequest("Malformed data: " + error.getMessage)
          }
      }
      case None => BadRequest("No data sent")
    }
  }

  def provision = Action { request =>
      if (request.remoteAddress == "127.0.0.1") {
        val (ok, message) = DatabaseStorage.provision(config)
        if (ok) Ok(message + "\n") else InternalServerError(message + "\n")
      } else {
        Unauthorized("Forbidden. Provisioning allowed only from local\n")
      }
  }

  private def storePersistable(persistable: Persistable, hash: String) = try {
    withDB { db =>
      val user = DatabaseStorage.storeToDb(db, persistable.withStorage(new Storage("", hash)))
      val id = user.getId.toString.substring(1)
      val response = Pickle.intoString(Saved(success = true, id, hash))
      Ok(response)
    }
  } catch {
    case e: Exception =>
      logDatabaseLoginContext(e)
      InternalServerError("Cannot connect to database\n")
  }


  private def logDatabaseLoginContext(e: Throwable): Unit = {
    val hashedPassword = config.getString("orientdb.password").getOrElse("").replaceAll(".", "*")
    val context =
      s"""
         |Cannot connect to database. Tried:
         |orientdb.path=${config.getString("orientdb.path")}
         |orientdb.db.main=${config.getString("orientdb.db.main")}
         |orientdb.user=${config.getString("orientdb.user")}
         |orientdb.password(obfuscated)=$hashedPassword
        """.stripMargin
      Logger.error(context, e)
  }

  private def withDB[A](online: DB => A)(implicit failure: A = InternalServerError("Cannot connect to database")): A = {
    DatabaseStorage.connection(config) match {
      case Some(db) => online(db)
      case None => failure
    }
  }
}
