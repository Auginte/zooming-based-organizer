package com.auginte.desktop

import javafx.beans.property.StringProperty
import scala.language.implicitConversions

/**
 * Extensions for JavaFx and ScalaFx.
 *
 * ScalaFx is designed using Strategy and Adapter patters.
 * This package have similar goals as ScalaFx, but designed using Template method pattern.
 *
 * So there are more freedom while extending JavaFx classes and manipulating them as child nodes.
 *
 * There are 3 types of elements:
   - Rich* traits - high abstraction functionality compatible with both: ScalaFx and JavaFx classes
   - RichJ* classes - ready to use JavaFx classes with Rich functionality
   - RichS* classes - ready to use ScalaFx classes with Rich functionality
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
package object rich {

  object Implicits {
    implicit def stringProperty2String(s: StringProperty) = s.get()
  }

}
