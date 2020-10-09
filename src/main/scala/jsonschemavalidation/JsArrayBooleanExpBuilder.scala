package jsonschemavalidation

import org.nulluncertainty.assertion.AssertionBuilder.assertThat
import org.nulluncertainty.expression.{Bool, ComposableBooleanExp, TrueBooleanExp, TrueExp}
import play.api.libs.json.{JsArray, JsObject, JsValue}

object JsArrayBooleanExpBuilder extends BooleanExpBuilder {

  override def build(path: Path, arrayDefinition: JsObject): ComposableBooleanExp[JsValue] = {

    val array: JsValue => Seq[JsValue] = path.valueIn(_).get.as[JsArray].value.toSeq

    val itemDefinition = (arrayDefinition \ "items").as[JsObject]

    ((arrayDefinition \ "minItems").asOpt[Int].map(minItems =>
      assertThat({array(_:JsValue).size}).isGreaterThanOrEqualTo(minItems).expression).toList ++
      (arrayDefinition \ "maxItems").asOpt[Int].map(maxItems =>
        assertThat({array(_:JsValue).size}).isLessThanOrEqualTo(maxItems).expression).toList ++
      (arrayDefinition \ "uniqueItems").asOpt[Boolean].map(_ =>
        assertThat(array).containsNoDuplicates.expression).toList ++
      (arrayDefinition \ "contains").asOpt[JsObject].map(containsDefinition =>
        new ComposableBooleanExp[JsValue] {
          override def evaluate(context: JsValue): Bool =
            array(context).zipWithIndex.foldLeft[Bool](TrueExp) {
              case (bool,(_, index)) =>
                bool.or(DelegatingJsBooleanExpBuilder.build(path.add(index), containsDefinition).evaluate(context))
            }
        }).toList :+
      new ComposableBooleanExp[JsValue] {
        override def evaluate(context: JsValue): Bool =
          array(context).zipWithIndex.foldLeft[Bool](TrueExp) {
            case (bool, (_, index)) =>
              bool.and(DelegatingJsBooleanExpBuilder.build(path.add(index), itemDefinition).evaluate(context))
          }
      })
      .reduceOption(_ and _)
      .getOrElse(TrueBooleanExp())
  }
}
