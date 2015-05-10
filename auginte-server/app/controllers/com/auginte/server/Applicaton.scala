package controllers.com.auginte.server

import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Testas"))
  }

  def store = Action {
    NotImplemented("Not implemented yet")
  }

}
