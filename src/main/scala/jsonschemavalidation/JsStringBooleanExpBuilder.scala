package jsonschemavalidation

import org.nulluncertainty.assertion.AssertionBuilder.assertThat
import org.nulluncertainty.expression.{ComposableBooleanExp, TrueBooleanExp}
import play.api.libs.json.{JsArray, JsObject, JsValue}

object JsStringBooleanExpBuilder extends BooleanExpBuilder {

  def build(path: Path, propertyDefinition: JsObject): ComposableBooleanExp[JsValue] = {

    val string: JsValue => String = path.valueIn(_).get.as[String]

    (propertyDefinition.\("enum").asOpt[JsArray], propertyDefinition.\("const").asOpt[String]) match {
      case (Some(enum), None) =>
        assertThat({_:JsValue => enum.value.map(_.as[String])}).contains(string).expression
      case (None, Some(const)) =>
        assertThat(string).isEqualTo(const).expression
      case (None, None) =>
        (propertyDefinition.\("minLength").asOpt[Int]
          .map(minLength => assertThat(string.andThen(_.trim)).isLongerThanOrEqualTo(minLength).expression).toList ++
          propertyDefinition.\("maxLength").asOpt[Int]
            .map(maxLength => assertThat(string.andThen(_.trim)).isShorterThanOrEqualTo(maxLength).expression).toList ++
          propertyDefinition.\("pattern").asOpt[String]
            .map(pattern => assertThat(string).matches(pattern).expression).toList ++
          propertyDefinition.\("format").asOpt[String]
            .map {
              case "uri" => assertThat(string).matches("https?:\\/\\/(localhost|(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,4})\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)").expression
            })
          .reduceOption(_ and _)
          .getOrElse(TrueBooleanExp())
    }
  }
}
