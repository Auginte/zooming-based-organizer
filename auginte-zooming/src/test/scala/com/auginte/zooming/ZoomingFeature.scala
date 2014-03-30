package com.auginte.zooming

import com.auginte.test.ModuleSpec

/**
 * Feature specification for zooming module
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class ZoomingFeature extends ModuleSpec {

  feature("Infinity zooming") {
    inOrderTo("easily navigate trough complex and heterogeneous data")
    asA("analytic")
    iNeed("fast zooming based interface without zooming limitations")

    scenario("Navigating from abstract to detailed structure") {
      Given("there are very large object and very small its parts")
      When("I zoom in, to see some parts in more detail")
      And("add new part near others")
      Then("position precision of added part should be same as of old")
      pending
    }

    scenario("Navigating from detailed to abstract structure") {
      Given("there are very large object and very small its parts")
      When("I zoom out, to see all parts")
      And("add new big bart near others")
      Then("position precision of added part should be same as of old")
      pending
    }

    scenario("Navigating horizontally and vertically") {
      Given("there are very large object and very small its parts")
      When("move grid long distance")
      And("add new element far from others")
      Then("position precision of added element should be same as of old")
      pending
    }


    inOrderTo("not loose data integrity")
    asA("developer")
    iNeed("elements to be attached to infinity grid")

    scenario("Camera should be attached to by current zoom and position")(pending)

    scenario("New elements should be attached by closest zoom and position")(pending)
  }
}
