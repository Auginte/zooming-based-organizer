package com.auginte.scalajs.communication

import com.auginte.scalajs.jshelpers.URIHelper
import com.auginte.scalajs.state.{Storage, State}
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.XMLHttpRequest
import prickle.{Unpickle, Pickle}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success, Try}

/**
 * Dependency injection for communication: asynchronous JavaScript and JSON
 */
trait Ajaj {

  protected def persist(state: State)(onResponse: (XMLHttpRequest, Try[Saved]) => Unit): Unit = {
    val hash = state.storage.hash
    val serialisedState = Pickle.intoString(state)

    Ajax.post(
      s"/demo/save/$hash",
      URIHelper.encodeURI(s"data=$serialisedState"),
      0,
      Map("Content-type" -> "application/x-www-form-urlencoded")
    ).onSuccess{ case response =>
      onResponse(response, Unpickle[Saved].fromString(response.responseText))
    }
  }

  protected def redirect(storage: Storage): Unit =
    dom.window.location.replace(s"/demo/load/${storage.id}/${storage.hash}")

}
