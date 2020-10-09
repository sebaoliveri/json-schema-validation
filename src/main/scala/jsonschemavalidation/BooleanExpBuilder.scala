package jsonschemavalidation

import org.nulluncertainty.expression.ComposableBooleanExp
import play.api.libs.json.{JsObject, JsValue}

trait BooleanExpBuilder {

  def build(path: Path, propertyDefinition: JsObject): ComposableBooleanExp[JsValue]
}
