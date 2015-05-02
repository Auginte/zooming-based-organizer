package com.auginte.scalajs

import com.auginte.scalajs.state.persistable.Storage
import japgolly.scalajs.react.React
import scala.scalajs.js
import org.scalajs.dom
import js.Dynamic.{global => g}

/**
 * Launching React application in Dom element
 *
 * Assuming:
 * {{
 *  <script type="text/javascript">
 *    var auginteInput = '{"#id": "15", "camera": ...}';
 *    var auginteHash = '832a5408e9ae19fb62777046c802d56d';
 *    var auginteId = 123;
 *  </script>
 * }}
 */
object Main extends js.JSApp {
  def main(): Unit = {
    val auginteInput = getDynamic(g.auginteInput, "")
    val auginteHash = getDynamic(g.auginteHash, "")
    val auginteId = getDynamic(g.auginteId, -1)

    val engine = new Page(auginteInput, Storage(auginteId, auginteHash))

    React.initializeTouchEvents(shouldUseTouch = true)
    React.render(engine.render(), dom.document.getElementById("container"))
  }

  private def getDynamic[A](variable: Dynamic, default: A): A =
    if(variable.isInstanceOf[Unit]) default else variable.asInstanceOf[A]
}
