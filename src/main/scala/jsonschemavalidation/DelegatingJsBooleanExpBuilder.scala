package jsonschemavalidation

import org.nulluncertainty.expression.ComposableBooleanExp
import play.api.libs.json.{JsArray, JsObject, JsValue}
import play.api.libs.json.Reads._

object DelegatingJsBooleanExpBuilder extends BooleanExpBuilder {

  val builders: Map[String,BooleanExpBuilder] =
    Map(
      "object" -> new JsExpectedTypeBooleanExpBuilder[JsObject](JsObjectReads, JsObjectBooleanExpBuilder),
      "string" -> new JsExpectedTypeBooleanExpBuilder[String](StringReads, JsStringBooleanExpBuilder),
      "array" -> new JsExpectedTypeBooleanExpBuilder[JsArray](JsArrayReads, JsArrayBooleanExpBuilder),
      "integer" -> new JsExpectedTypeBooleanExpBuilder[Int](IntReads, JsNumericBooleanExpBuilder),
      "number" -> new JsExpectedTypeBooleanExpBuilder[BigDecimal](bigDecReads, JsNumericBooleanExpBuilder)
    )

  override def build(path: Path, definition: JsObject): ComposableBooleanExp[JsValue] =
    builders(definition.\("type").as[String]).build(path, definition)
}
