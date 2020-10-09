package jsonschemavalidation

import org.nulluncertainty.assertion.AssertionBuilder.assertThat
import org.nulluncertainty.expression.{ComposableAssertionExp, SuccessfulAssertionExp}
import play.api.libs.json.{JsArray, JsObject, JsValue}

object JsNumericValidationBuilder extends JsValidationBuilder {

  override def build(path: Path, propertyDefinition: JsObject): ComposableAssertionExp[JsValue,JsValue,JsValue] = {

    val number: JsValue => BigDecimal = path.valueIn(_).get.as[BigDecimal]

    (propertyDefinition.\("enum").asOpt[JsArray], propertyDefinition.\("const").asOpt[BigDecimal]) match {
      case (Some(enum), None) =>
        assertThat({ _: JsValue => enum.value.toSeq.map(_.as[BigDecimal]) }).contains(number)
          .otherwise[String](s"Property ${path.toString} does not match any of [${enum.value.toSeq.map(_.as[BigDecimal]).mkString(", ")}]")
      case (None, Some(const)) =>
        assertThat(number).isEqualTo(const).otherwise[String](s"${path.toString} is not equal to $const")
      case (None, None) =>
        (propertyDefinition.\("maximum").asOpt[Int]
          .map(maximum => assertThat(number).isLessThanOrEqualTo(maximum)
            .otherwise[String](s"${path.toString} exceeds maximum value of $maximum")).toList ++
          propertyDefinition.\("minimum").asOpt[Int]
            .map(minimum => assertThat(number).isGreaterThanOrEqualTo(minimum)
              .otherwise[String](s"${path.toString} is smaller than required minimum value of $minimum")).toList ++
          propertyDefinition.\("exclusiveMaximum").asOpt[Int]
            .map(exclusiveMaximum => assertThat(number).isLessThan(exclusiveMaximum)
              .otherwise[String](s"${path.toString} exceeds exclusive maximum value of $exclusiveMaximum")).toList ++
          propertyDefinition.\("exclusiveMinimum").asOpt[Int]
            .map(exclusiveMinimum => assertThat(number).isGreaterThan(exclusiveMinimum)
              .otherwise[String](s"${path.toString} is smaller than required exclusive minimum value of $exclusiveMinimum")))
          .reduceOption[ComposableAssertionExp[JsValue,JsValue,JsValue]](_ ifTrue _)
          .getOrElse(SuccessfulAssertionExp())
    }
  }
}
