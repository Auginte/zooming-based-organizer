package com.auginte.desktop.persistable

import com.auginte.desktop.rich.RichJPane
import com.auginte.distribution.orientdb.ReferConnection

/**
 * Refer edge with GUI elements as wrappers
 */
object VisualReferConnection {
  def unapply(connection: ReferConnection): Option[(RichJPane, RichJPane, Int)] = connection.from match {
    case from: RichJPane => connection.to match {
      case to: RichJPane => Some(from, to, connection.distance)
      case _ => None
    }
    case _ => None
  }
}
