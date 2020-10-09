package jsonschemavalidation

import org.nulluncertainty.expression.{ComposableAssertionExp, SuccessfulAssertionExp}
import play.api.libs.json.{JsObject, JsValue}

object JsNullValidationBuilder extends JsValidationBuilder {
  override def build(path: Path, propertyDefinition: JsObject): ComposableAssertionExp[JsValue,JsValue,JsValue] =
    SuccessfulAssertionExp()
}
