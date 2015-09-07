package com.auginte.users

import akka.actor.{Actor, _}
import akka.util.Timeout
import spray.can.Http
import spray.http.HttpMethods._
import spray.http.{HttpRequest, HttpResponse, _}

import scala.concurrent.duration._
import scala.io.Source

/**
 * User actor
 */
class Users extends Actor with ActorLogging {
  implicit val timeout: Timeout = 2.second // ExecutionContext for the futures and scheduler

  lazy val style = Source.fromInputStream(getClass.getResourceAsStream("/css/testas.css")).mkString

  override def receive: Receive = {
    case _: Http.Connected => sender ! Http.Register(self)

    case HttpRequest(GET, Uri.Path("/test.css"), _, _, _) =>
      sender() ! HttpResponse(entity = style)

    case HttpRequest(GET, _, _, _, _) =>
      sender ! HttpResponse(entity = html(usersPage))
  }

  def html(text: String) = HttpEntity(ContentType(MediaTypes.`text/html`, HttpCharsets.`UTF-8`), doctype + text)

  val doctype = """<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">"""

  lazy val usersPage = s"""<html>
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
      <style type="text/css">
        $style
      </style>
      <title>Users</title>
    </head>
    <body>
      User registration
    </body>
  </html>"""
}
