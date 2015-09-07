package com.auginte.users

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http

import scala.concurrent.duration._

/**
 * Main class
 */
object Main extends App {
  implicit val system = ActorSystem()

  implicit val timeout = Timeout(5.seconds)

  // the handler actor replies to incoming HttpRequests
  val handler = system.actorOf(Props[Users], name = "handler")

  IO(Http) ? Http.Bind(handler, interface = "localhost", port = 9999)
}
