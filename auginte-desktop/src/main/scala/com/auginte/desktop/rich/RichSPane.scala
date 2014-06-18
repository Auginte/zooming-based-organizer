package com.auginte.desktop.rich

import scalafx.scene.{layout => sfxl}
import javafx.scene.{layout => jfxl}
import javafx.scene.{input => jfxi}
import javafx.scene.{control => jfxc}
import javafx.{scene => jfxs}

/**
 * Extending ScalaFx Pane with RichNode functionality
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class RichSPane extends sfxl.Pane with RichNodeGeneralEvents[jfxl.Pane] {
  protected[desktop] val d = delegate
}
