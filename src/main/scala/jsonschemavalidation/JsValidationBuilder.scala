package jsonschemavalidation

import org.nulluncertainty.expression.ComposableAssertionExp
import play.api.libs.json.{JsObject, JsValue}

trait JsValidationBuilder {

  def build(path: Path, propertyDefinition: JsObject): ComposableAssertionExp[JsValue,JsValue,JsValue]
}
