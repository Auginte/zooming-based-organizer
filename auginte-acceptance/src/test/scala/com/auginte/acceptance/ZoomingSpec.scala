package com.auginte.acceptance

import org.scalatest.WordSpec

/**
 * Testing, if element and plane can be moved
 */
class ZoomingSpec extends WordSpec with DebugHelper with BrowserHelper with ReactHelper with MouseHelper {
  "For main functionality I " should {
    "be able to zoom my area" when {
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
      "zoom out view" in {
        val heightBefore = element("span.dragable").getSize.getHeight
        wheel(53, ".area")
        val heightAfter = element("span.dragable").getSize.getHeight
        assert(heightBefore > heightAfter)
      }
      "zoom in view" in {
        val heightBefore = element("span.dragable").getSize.getHeight
        wheel(-53, ".area")
        val heightAfter = element("span.dragable").getSize.getHeight
        assert(heightBefore < heightAfter)
      }
      "close browser" in {
        driver.quit()
      }
    }
  }
}
