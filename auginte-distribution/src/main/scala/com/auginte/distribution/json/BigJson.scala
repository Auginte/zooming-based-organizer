package com.auginte.distribution.json

import java.io.{BufferedInputStream, InputStream}

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.{JsonNode, MappingJsonFactory}
import play.api.libs.json.Json

/**
 * Wrapper for [[com.fasterxml.jackson]] to read large JSON files without consuming RAM.
 *
 * Parsing only files of fist-element-object and 2 levels of data
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
class BigJson {
  def read(input: InputStream, callback: JsonTagEvent => Boolean): Unit = {
    val f = new MappingJsonFactory
    val jp = f.createParser(input)

    var continue = jp.nextToken() == JsonToken.START_OBJECT
    while (continue && jp.nextToken() != JsonToken.END_OBJECT) {
      val fieldName = jp.getCurrentName
      continue = jp.nextToken() match {
        case JsonToken.START_ARRAY =>
          var continueArray = true
          while (continueArray && jp.nextToken() != JsonToken.END_ARRAY) {
            continueArray = callback(JsonTagEvent(Array, fieldName, jp.readValueAsTree[JsonNode].toString))
          }
          continueArray
        case JsonToken.START_OBJECT =>
          callback(JsonTagEvent(Object, fieldName, jp.readValueAsTree[JsonNode].toString))
        case _ =>
          callback(JsonTagEvent(Value, fieldName, jp.getValueAsString))
      }
    }
  }

}
