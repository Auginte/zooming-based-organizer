package com.auginte.acceptance

import org.openqa.selenium.Point

trait ReactHelper extends BrowserHelper {
  def waitForReact() = {
    waitXpath("""//div[@data-reactid=".0"]""")
  }

  protected def touchEventScript(reactId: String, eventName: String, cursor: Point) =
    s"""
       |var element = document.querySelector('[data-reactid="$reactId"]');
       |var event = {touches: [{screenX: ${cursor.x}, screenY: ${cursor.y}}], changedTouches: [{screenX: ${cursor.x}, screenY: ${cursor.y}}]}
       |React.addons.TestUtils.Simulate.$eventName(element, event)
    """.stripMargin
}
