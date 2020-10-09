package jsonschemavalidation

import org.nulluncertainty.assertion.AssertionBuilder.assertThat
import org.nulluncertainty.expression.{ComposableAssertionExp, SuccessfulAssertionExp}
import play.api.libs.json.{JsArray, JsObject, JsValue}

object JsStringValidationBuilder extends JsValidationBuilder {

  override def build(path: Path, propertyDefinition: JsObject): ComposableAssertionExp[JsValue,JsValue,JsValue] = {

    val string: JsValue => String = path.valueIn(_).get.as[String]

    (propertyDefinition.\("enum").asOpt[JsArray], propertyDefinition.\("const").asOpt[String]) match {
      case (Some(enum), None) =>
        assertThat({ _: JsValue => enum.value.toSeq.map(_.as[String]) }).contains(string)
          .otherwise[String](s"Property ${path.toString} does not match any of [${enum.value.toSeq.map(_.as[String]).mkString(", ")}]")
      case (None, Some(const)) =>
        assertThat(string).isEqualTo(const)
          .otherwise[String](s"${path.toString} is not equal to $const")
      case (None, None) =>
        (propertyDefinition.\("minLength").asOpt[Int]
          .map(minLength => assertThat(string.andThen(_.trim)).isLongerThanOrEqualTo(minLength)
            .otherwise[String](s"Property ${path.toString} does not match minimum length of $minLength")).toList ++
          propertyDefinition.\("maxLength").asOpt[Int]
            .map(maxLength => assertThat(string.andThen(_.trim)).isShorterThanOrEqualTo(maxLength)
              .otherwise[String](s"Property ${path.toString} exceeds maximum length of $maxLength")).toList ++
          propertyDefinition.\("pattern").asOpt[String]
            .map(pattern => assertThat(string).matches(pattern)
              .otherwise[String](s"Property ${path.toString} does not match pattern $pattern")).toList ++
          propertyDefinition.\("format").asOpt[String]
            .map {
              case "uri" => assertThat(string).matches("https?:\\/\\/(localhost|(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,4})\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)")
                .otherwise[String](s"Property ${path.toString} is not an URI")
              case "money" => assertThat(string).matches("^\\d*\\.?\\d{1,2}$")
                .otherwise[String](s"Property ${path.toString} should be money represented as a big decimal having optional 2 decimals at most")
            })
          .reduceOption[ComposableAssertionExp[JsValue,JsValue,JsValue]](_ ifTrue _)
          .getOrElse(SuccessfulAssertionExp())
    }
  }
}
