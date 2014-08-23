package com.auginte.distribution.json

import java.io.{BufferedInputStream, InputStream}

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.{JsonNode, MappingJsonFactory}
import play.api.libs.json.Json

/**
 * Wrapper for [[com.fasterxml.jackson]] to read large JSON files without consuming RAM
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class BigJson {
  def read(input: InputStream, callback: JsonTagEvent => Boolean): Unit = {
    val f = new MappingJsonFactory
    val jp = f.createParser(input)
    jp.nextToken()

    while (jp.nextToken() != JsonToken.END_OBJECT) {
      val fieldName = jp.getCurrentName
      jp.nextToken() match {
        case JsonToken.START_ARRAY =>
          while (jp.nextToken() != JsonToken.END_ARRAY) {
            val continue = callback(JsonTagEvent(Array, fieldName, jp.readValueAsTree[JsonNode].toString))
          }
        case JsonToken.START_OBJECT =>
          val continue = callback(JsonTagEvent(Object, fieldName, jp.readValueAsTree[JsonNode].toString))
        case _ =>
          val continue = callback(JsonTagEvent(Value, fieldName, jp.getValueAsString))
      }
    }
  }


}
