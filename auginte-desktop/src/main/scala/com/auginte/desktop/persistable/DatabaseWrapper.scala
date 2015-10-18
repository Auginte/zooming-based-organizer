package com.auginte.desktop.persistable

import com.auginte.distribution.orientdb.Persistable
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph

/**
 * Saving link to database over different threads.
 */
trait DatabaseWrapper {
  private var _db: Option[OrientBaseGraph] = None

  protected def db = _db

  protected def db_=(db: OrientBaseGraph): Unit = _db = Some(db)

  protected def attachDatabaseToElement(element: Persistable[_]): Unit = db match {
    case Some(db) => element.persisted match {
      case Some(persisted) if persisted.getGraph == null =>
        persisted.attach(db)
      case other => Unit
    }
    case None => Unit
  }
}
