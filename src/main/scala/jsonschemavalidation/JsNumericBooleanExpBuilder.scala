package jsonschemavalidation

import org.nulluncertainty.assertion.AssertionBuilder.assertThat
import org.nulluncertainty.expression.{ComposableBooleanExp, TrueBooleanExp}
import play.api.libs.json.{JsArray, JsObject, JsValue}

object JsNumericBooleanExpBuilder extends BooleanExpBuilder {

  override def build(path: Path, propertyDefinition: JsObject): ComposableBooleanExp[JsValue] = {

    val number: JsValue => BigDecimal = path.valueIn(_).get.as[BigDecimal]

    (propertyDefinition.\("enum").asOpt[JsArray], propertyDefinition.\("const").asOpt[BigDecimal]) match {
      case (Some(enum), None) =>
        assertThat({_:JsValue => enum.value.map(_.as[BigDecimal])}).contains(number).expression
      case (None, Some(const)) =>
        assertThat(number).isEqualTo(const).expression
      case (None, None) =>
        (propertyDefinition.\("maximum").asOpt[Int]
          .map(maximum => assertThat(number).isLessThanOrEqualTo(maximum).expression).toList ++
          propertyDefinition.\("minimum").asOpt[Int]
            .map(minimum => assertThat(number).isGreaterThanOrEqualTo(minimum).expression).toList ++
          propertyDefinition.\("exclusiveMaximum").asOpt[Int]
            .map(exclusiveMaximum => assertThat(number).isLessThan(exclusiveMaximum).expression).toList ++
          propertyDefinition.\("exclusiveMinimum").asOpt[Int]
            .map(exclusiveMinimum => assertThat(number).isGreaterThan(exclusiveMinimum).expression))
          .reduceOption(_ and _)
          .getOrElse(TrueBooleanExp())
    }
  }
}
