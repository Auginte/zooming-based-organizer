package com.auginte.desktop

import javafx.{scene => jfxs}
import com.auginte.distribution.orientdb.RepresentationWrapper

/**
 * Functionality related to storing and loading from Database.
 */
package object persistable {
  type GuiRepresentation = jfxs.Node with RepresentationWrapper with ViewWrapper
}
