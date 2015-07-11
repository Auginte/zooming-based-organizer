package com.auginte.acceptance

import org.scalatest.WordSpec

/**
 * Testing, if element can be deleted
 */
class DeleteElementSpec extends WordSpec with DebugHelper with BrowserHelper with ReactHelper with TouchHelper with SelectionStyleHelper {
  "For main functionality I " should {
    "be able to remove element" when {
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
      "not see selection operations in sidebar menu" in {
        element("div.sidebar button.btn-context").click()
        noElement("div.sidebar div.selected-sidebar")
        element("div.sidebar button.btn-context").click()
      }
      "select element" in {
        val element1 = element("span.dragable")
        element1.click()
        elementRenderedSelected(element1)
      }
      "see selection operations in sidebar menu" in {
        element("div.sidebar button.btn-context").click()
        assert(element("div.sidebar div.selected-sidebar").isDisplayed)
      }
      "click delete operation" in {
        element("div.sidebar div.selected-sidebar button.acte-delete-selected").click()
      }
      "sidebar is hidden" in {
        assert(!element("div.sidebar .sidebar-items").isDisplayed)
      }
      "element is removed" in {
        noElement("span.dragable")
      }
      "close browser" in {
        driver.quit()
      }
    }
  }
}
