package jsonschemavalidation

import org.nulluncertainty.expression.{BooleanExp, ComposableBooleanExp}
import play.api.libs.json.{JsObject, JsValue, Reads}

import scala.reflect.ClassTag

class JsExpectedTypeBooleanExpBuilder[U: ClassTag](reads: Reads[U], next: BooleanExpBuilder) extends BooleanExpBuilder {

  override def build(path: Path, propertyDefinition: JsObject): ComposableBooleanExp[JsValue] = {
    val propertyValueIsExpectedType: JsValue => Boolean = path.valueIn(_).get.validate[U](reads).isSuccess

    BooleanExp({propertyValueIsExpectedType(_)}).isTrue
      .ifTrue(next.build(path, propertyDefinition))
  }
}
