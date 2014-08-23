package com.auginte.distribution.json

/**
 * Event/CallBack for [[com.fasterxml.jackson.core.JsonToken]]
 *
 * Which is used with Jackson Streaming API
 *
 * @param event type of value. Using same tag name for array elements
 * @param tagName key name of json. E.g. `"key":"value"`, `"key":[]`, `"key":{"va":"lue"}`
 * @param rawValue depends on `event`. E.g. `"plain value"`, `{"array":"element"}`, `{"whole":"object"}`
 *
 * @author Aurelijus Banelis <aurelijus@banelis.lt>
 */
case class JsonTagEvent(event: JsonTagType, tagName: String, rawValue: String)

sealed trait JsonTagType
case object Value extends JsonTagType
case object Object extends JsonTagType
case object Array extends JsonTagType