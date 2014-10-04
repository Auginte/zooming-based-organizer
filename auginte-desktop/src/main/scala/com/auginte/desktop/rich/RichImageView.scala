package com.auginte.desktop.rich

import javafx.beans.property.DoubleProperty
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.ObservableList
import javafx.scene.image.ImageView
import javafx.scene.{input => jfxi, layout => jfxl}
import javafx.{event => jfxe}

import scala.language.implicitConversions
import scalafx.scene.{input => sfxi}

/**
 * Delegating Rich functionality to JavaFx ImageView
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class RichImageView extends ImageView with RichNode[ImageView] {
  override val d = this
}
