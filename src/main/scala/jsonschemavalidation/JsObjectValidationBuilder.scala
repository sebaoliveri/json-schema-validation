package jsonschemavalidation

import org.nulluncertainty.assertion.AssertionBuilder.assertThat
import org.nulluncertainty.expression.{BooleanExp, ComposableAssertionExp, SuccessfulAssertionExp}
import play.api.libs.json.{JsArray, JsNull, JsObject, JsValue}

object JsObjectValidationBuilder extends JsValidationBuilder {

  override def build(path: Path, objectDefinition: JsObject): ComposableAssertionExp[JsValue,JsValue,JsValue] = {

    val buildFrom: JsObject => ComposableAssertionExp[JsValue,JsValue,JsValue] = schema => {

      val allProperties: collection.Map[String, JsValue] =
        (schema \ "properties").as[JsObject].value

      val requiredProperties: collection.Seq[String] =
        (schema \ "required").asOpt[JsArray]
          .map(_.value.map(_.as[String]))
          .getOrElse(Nil)

      allProperties.keys.partition(requiredProperties.contains) match {
        case (requiredProperties, nonRequiredProperties) =>
          requiredProperties.foldLeft[ComposableAssertionExp[JsValue,JsValue,JsValue]](SuccessfulAssertionExp()) {
            case (assertions, requiredProperty) =>
              assertions.and(
                assertThat({ js: JsValue => path.add(requiredProperty).valueIn(js).exists(_ != JsNull) }).isTrue
                  .otherwise[String](s"Property ${path.add(requiredProperty).toString} is missing")
                  .ifTrue(DelegatingJsValidationBuilder.build(path.add(requiredProperty), allProperties(requiredProperty).as[JsObject])))
          }
          .and(
            nonRequiredProperties.foldLeft[ComposableAssertionExp[JsValue,JsValue,JsValue]](SuccessfulAssertionExp()) {
              case (assertions, nonRequiredProperty) =>
                assertions.and(
                  BooleanExp({ js: JsValue => path.add(nonRequiredProperty).valueIn(js).exists(_ != JsNull) }).isTrue
                    .thenElse(
                      (context: JsValue) => {
                        DelegatingJsValidationBuilder.build(path.add(nonRequiredProperty), allProperties(nonRequiredProperty).as[JsObject]).evaluate(context)
                      },
                      SuccessfulAssertionExp()))
            }
          )
      }
    }

    val maybeAllOf: Option[ComposableAssertionExp[JsValue,JsValue,JsValue]] =
      (objectDefinition \ "allOf").asOpt[JsArray].map { _.value.map { conditionalSubschemas =>
        DelegatingJsBooleanExpBuilder.build(path, (conditionalSubschemas \ "if").as[JsObject])
          .thenElse(
            buildFrom((conditionalSubschemas \ "then").as[JsObject]),
            (conditionalSubschemas \ "else").asOpt[JsObject].map(buildFrom).getOrElse(SuccessfulAssertionExp()))
      }.reduce[ComposableAssertionExp[JsValue,JsValue,JsValue]](_ and _)
      }

    (maybeAllOf.toList :+ buildFrom(objectDefinition)).reduce(_ and _)
  }
}
