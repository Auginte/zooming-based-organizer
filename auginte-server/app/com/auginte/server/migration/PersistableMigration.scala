package com.auginte.server.migration

import com.auginte.shared.Version
import microjson._

object PersistableMigration {
  def migrate(original: String): String = {
    val updated = Json.read(original).value match {
      case j: Map[String, JsObject] if j.contains("container") => j("container").value match {
        case c: Map[String, JsObject] if c.contains("elements") => c("elements").value match {
          case ce: Map[String, JsArray] if ce.contains("#elems") => ce("#elems").value match {
            case elems: Seq[JsArray] =>
              val updatedElements = elems.map { e =>
                e.value(1).value match {
                  case data: Map[String, JsValue] =>
                    JsArray(List(e.value.head, JsObject(data.updated("scale", JsNumber("1.0")))))
                  case _ => e
                }
              }
              val main = j.updated("version", JsString(Version.textual))
              Json.write(
                JsObject(main.updated("container",
                  JsObject(c.updated("elements",
                    JsObject(ce.updated("#elems",
                      JsArray(updatedElements))
                    ))
                  ))
                ))
            case _ => original
          }
          case _ => original
        }
        case _ => original
      }
      case _ => original
    }
    updated
  }

}
