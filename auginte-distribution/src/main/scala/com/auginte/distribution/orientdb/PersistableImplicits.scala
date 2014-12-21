package com.auginte.distribution.orientdb

import com.tinkerpop.blueprints.impls.orient.{OrientBaseGraph, OrientVertex}

/**
 * Implicits for easier code generation.
 */
object PersistableImplicits {
  implicit class BoxedBool(val d: Boolean) extends AnyVal {
    def boxed = Boolean.box(d)
  }

  implicit class BoxedInt(val d: Int) extends AnyVal {
    def boxed: Object = Int.box(d)
  }

  implicit class BoxedFloat(val d: Float) extends AnyVal {
    def boxed: Object = Float.box(d)
  }

  implicit class BoxedDouble(val d: Double) extends AnyVal {
    def boxed: Object = Double.box(d)
  }

  implicit class BoxedString(val d: String) extends AnyVal {
    def boxed: Object = d
  }
}