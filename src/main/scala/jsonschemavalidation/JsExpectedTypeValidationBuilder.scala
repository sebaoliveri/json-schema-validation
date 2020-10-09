package jsonschemavalidation

import org.nulluncertainty.assertion.AssertionBuilder.assertThat
import org.nulluncertainty.expression.ComposableAssertionExp
import play.api.libs.json.{JsObject, JsValue, Reads}

import scala.reflect.{ClassTag, classTag}

class JsExpectedTypeValidationBuilder[U: ClassTag](reads: Reads[U], next: JsValidationBuilder) extends JsValidationBuilder {

  override def build(path: Path, propertyDefinition: JsObject): ComposableAssertionExp[JsValue,JsValue,JsValue] = {

    val propertyValueIsExpectedType: JsValue => Boolean = path.valueIn(_).get.validate[U](reads).isSuccess

    assertThat(propertyValueIsExpectedType(_)).isTrue
      .otherwise[String](s"${path.toString} is expected to be ${classTag[U].runtimeClass.getSimpleName}")
      .ifTrue(next.build(path, propertyDefinition))
  }
}
