package com.auginte.desktop.rich

import scalafx.scene.{input => sfxi}
import javafx.scene.{input => jfxi}
import javafx.{scene => jfxs}

/**
 * Alternative to ScalaFx Node.
 *
 * Compatible with both: ScalaFX and JavaFx
 * Used to add functionality to JavaFx nodes
 * and prevent implicit transformation.
 *
 *
 * @tparam D JavaFx class to use with `delegate` value
 *
 * @see [[scalafx.scene.Node]]
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
trait RichNode[D <: jfxs.Node] extends RichNodeDelegating[D] with RichNodeGeneralEvents[D]