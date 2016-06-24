package com.auginte.server.services

import javax.inject._

import com.auginte.server.storage.DatabaseStorage
import play.api.{Configuration, Logger}

import scala.annotation.tailrec

@Singleton
class Provisioning @Inject()(val config: Configuration) {
  private val retryCount = 20
  private val retryDelay = 250

  lazy val logger = Logger(getClass)

  /**
    * Is there environment variable asking to do provisioning
    */
  def askedToProvision: Boolean = System.getProperty("provision", "false").toLowerCase() match {
      case "true" | "on" | "1" => true
      case _ => false
  }

  @tailrec
  private def retry(count: Int)(fn: => Boolean): Boolean = {
    fn match {
      case true => true
      case false if count > 0 => retry(count - 1)(fn)
      case _ => false
    }
  }

  def provision(): Unit = {
    if (askedToProvision) {
      logger.debug("Will provision")
      new Thread(new Runnable {
        override def run(): Unit = {
          retry(retryCount) {
            val (ok, message) = DatabaseStorage.provision(config)
            if (!ok) {
              logger.info(s"Provisioning failed: $message")
              logger.info(s"Will retry in $retryDelay ms")
              Thread.sleep(retryDelay)
            } else {
              logger.debug(s"Provisioned: $message")
            }
            ok
          } match {
            case true => logger.info("Provision successful")
            case false => logger.error("Provisioning failed")
          }
        }
      }).start()
    }
  }
}
