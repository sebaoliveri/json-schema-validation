package jsonschemavalidation

import org.nulluncertainty.expression.{BooleanExp, ComposableBooleanExp, TrueBooleanExp, TrueExp}
import play.api.libs.json.{JsArray, JsNull, JsObject, JsValue}

object JsObjectBooleanExpBuilder extends BooleanExpBuilder {

  override def build(path: Path, objectDefinition: JsObject): ComposableBooleanExp[JsValue] = {

    val allProperties: collection.Map[String, JsValue] =
      (objectDefinition \ "properties").as[JsObject].value

    val requiredProperties: collection.Seq[String] =
      (objectDefinition \ "required").asOpt[JsArray]
        .map(_.value.map(_.as[String]))
        .getOrElse(Nil)

    allProperties.keys.partition(requiredProperties.contains) match {
      case (requiredProperties, nonRequiredProperties) =>
        requiredProperties.foldLeft[ComposableBooleanExp[JsValue]](TrueBooleanExp()) {
          case (boolExp, requiredProperty) =>
            boolExp.and(
              BooleanExp({ js: JsValue => path.add(requiredProperty).valueIn(js).exists(_ != JsNull) }).isTrue
                .and(DelegatingJsBooleanExpBuilder.build(path.add(requiredProperty), allProperties(requiredProperty).as[JsObject])))
        }
        .and(
          nonRequiredProperties.foldLeft[ComposableBooleanExp[JsValue]](TrueBooleanExp()) {
            case (boolExp, nonRequiredProperty) =>
              boolExp.and(
                (context: JsValue) =>
                  BooleanExp({ js: JsValue => path.add(nonRequiredProperty).valueIn(js).exists(_ != JsNull) }).isTrue
                    .evaluate(context)
                    .thenElse(
                      DelegatingJsBooleanExpBuilder.build(path.add(nonRequiredProperty), allProperties(nonRequiredProperty).as[JsObject]).evaluate(context),
                      TrueExp)
              )
          }
        )
    }
  }
}
