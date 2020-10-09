package jsonschemavalidation

import org.nulluncertainty.expression._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsObject, JsValue}

object JsValidation {

  def from(schema: JsValue): JsValidation =
    JsValidation(DelegatingJsValidationBuilder
      .build(
        Path(schema.\("title").asOpt[String]),
        schema.as[JsObject]))
}

case class JsValidation(schemaValidation: ComposableAssertionExp[JsValue,JsValue,JsValue]) {

  def appliedTo(json: JsValue): AssertionResultBehaviour[JsValue] =
    schemaValidation.evaluate(json)
}
