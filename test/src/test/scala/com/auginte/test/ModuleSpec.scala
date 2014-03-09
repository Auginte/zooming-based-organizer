package com.auginte.test

import org.scalatest.{GivenWhenThen, FeatureSpec}

/**
 * Base class for ScalaTests features.
 *
 * Behavior driven development.
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
abstract class ModuleSpec extends FeatureSpec with GivenWhenThen {
  def inOrderTo(addedValue: String) = info("In order to " + addedValue)

  def asA(role: String) = info("As a " + role)

  def iNeed(what: String) = info("I need " + what)
}
