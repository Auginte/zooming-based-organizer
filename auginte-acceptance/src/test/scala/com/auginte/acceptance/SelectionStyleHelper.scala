package com.auginte.acceptance

import org.openqa.selenium.WebElement

/**
 * Helpers to check if CSS class are updated when element is selected
 */
trait SelectionStyleHelper {
  private val classSelectedElement = "selected-element"
  private val classSelectedView = "selected-view"

  @inline
  def elementRenderedSelected(element: WebElement): Unit =
    assert(
      element.getAttribute("class").contains(classSelectedElement),
      s"Element class: ${element.getAttribute("class")} should have $classSelectedElement"
    )

  @inline
  def elementRenderedNotSelected(element: WebElement): Unit =
    assert(
      !element.getAttribute("class").contains(classSelectedElement),
      s"Element class: ${element.getAttribute("class")} should NOT have $classSelectedElement"
    )

  @inline
  def elementRenderedStillSelected(element: WebElement) = elementRenderedSelected(element)

  @inline
  def viewRenderedSelected(element: WebElement): Unit =
    assert(
      element.getAttribute("class").contains(classSelectedView),
      s"View class: ${element.getAttribute("class")} should have $classSelectedView"
    )

  @inline
  def viewRenderedNotSelected(element: WebElement): Unit =
    assert(
      !element.getAttribute("class").contains(classSelectedView),
      s"View class: ${element.getAttribute("class")} should NOT have $classSelectedView"
    )

  @inline
  def viewRenderedStillSelected(element: WebElement) = viewRenderedSelected(element)
}
