package jsonschemavalidation

import org.nulluncertainty.expression.ComposableAssertionExp
import play.api.libs.json.{JsArray, JsObject, JsValue}
import play.api.libs.json.Reads._

object DelegatingJsValidationBuilder extends JsValidationBuilder {

  val builders: Map[String,JsValidationBuilder] =
    Map(
      "object" -> new JsExpectedTypeValidationBuilder[JsObject](JsObjectReads, JsObjectValidationBuilder),
      "string" -> new JsExpectedTypeValidationBuilder[String](StringReads, JsStringValidationBuilder),
      "array" -> new JsExpectedTypeValidationBuilder[JsArray](JsArrayReads, JsArrayValidationBuilder),
      "integer" -> new JsExpectedTypeValidationBuilder[Int](IntReads, JsNumericValidationBuilder),
      "number" -> new JsExpectedTypeValidationBuilder[BigDecimal](bigDecReads, JsNumericValidationBuilder),
      "boolean" -> new JsExpectedTypeValidationBuilder[Boolean](BooleanReads, JsNullValidationBuilder),
    )

  override def build(path: Path, definition: JsObject): ComposableAssertionExp[JsValue,JsValue,JsValue] =
    builders(definition.\("type").as[String]).build(path, definition)
}
