package com.auginte.acceptance

import org.scalatest.WordSpec

/**
 * Testing, if saving of elements works
 */
class SavingSpec extends WordSpec with DebugHelper with BrowserHelper {
  "For minimal functionality I " should {
    "be able to save my work" when {
      "go to home page" in {
        driver.navigate().to(baseUrl)
      }
      "wait for GUI to load" in {
        waitXpath("""//div[@data-reactid=".0"]""")
        val loaded = element("input.form-control.input-field").getAttribute("placeholder")
        assert(loaded === "New element")
      }
      "create new element" in {
        element("input.form-control.input-field").sendKeys("Test")
        element(".main-nav .input-bar .btn-add").click()
      }
      "see newly created element" in {
        assert(waitVisible("span.dragable").getText === "Test")
      }
      "click save" in {
        element("div.sidebar button.btn-context").click()
        element("div.sidebar-items button").click()
      }
      "be redirected to new page" in {
        waitState("body")
        assert(driver.getCurrentUrl.contains("/load/"))
      }
      "still see saved element" in {
        assert(waitVisible("span.dragable").getText === "Test")
      }
    }
  }
}
