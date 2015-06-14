package com.auginte.acceptance

import org.scalatest.WordSpec

/**
 * Testing, if element and plane can be moved using touch screen
 */
class TouchMoveSpec extends WordSpec with DebugHelper with BrowserHelper with ReactHelper with TouchHelper {
  "For mobile functionality I " should {
    "be able to move my elements and area" when {
      "go to home page" in {
        driver.navigate().to(baseUrl)
      }
      "wait for GUI to load" in {
        waitForReact()
      }
      "create new element" in {
        element("input.form-control.input-field").sendKeys("Element1")
        element(".main-nav .input-bar .btn-add").click()
      }
      "see newly created element" in {
        val element1 = waitVisible("span.dragable")
        assert(element1.getText === "Element1")
      }
      "move element by touching" in {
        val element = waitVisible("span.dragable")
        val oldLocation = center(element)
        touchDown(element)
        touchMove(40, 50)
        touchUp()
        val newLocaltion = center(element)
        assert(oldLocation.x + 40 === newLocaltion.x)
        assert(oldLocation.y + 50 === newLocaltion.y)
      }
      "move plane" in {
        val element = waitVisible("span.dragable")
        val oldLocation = center(element)
        val plane = waitVisible(".area")
        touchDown(plane)
        touchMove(60, 70)
        touchUp()
        val newLocation = center(element)
        assert(oldLocation.x + 60 === newLocation.x)
        assert(oldLocation.y + 70 === newLocation.y)
      }
      "close browser" in {
        driver.quit()
      }
    }
  }
}
