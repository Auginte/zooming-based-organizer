package com.auginte.desktop.persistable

import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph

/**
 * Saving link to database over different threads.
 */
trait DatabaseWrapper {
  private var _db: Option[OrientBaseGraph] = None

  protected def db = _db

  protected def db_=(db: OrientBaseGraph): Unit = _db = Some(db)
}
