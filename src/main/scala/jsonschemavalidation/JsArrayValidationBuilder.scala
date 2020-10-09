package jsonschemavalidation

import org.nulluncertainty.assertion.AssertionBuilder.assertThat
import org.nulluncertainty.expression.{AssertionResultBehaviour, AssertionSuccessfulResult, ComposableAssertionExp, SuccessfulAssertionExp}
import play.api.libs.json.{JsArray, JsObject, JsValue}

object JsArrayValidationBuilder extends JsValidationBuilder {

  override def build(path: Path, arrayDefinition: JsObject): ComposableAssertionExp[JsValue,JsValue,JsValue] = {

    val array: JsValue => Seq[JsValue] = path.valueIn(_).get.as[JsArray].value.toSeq

    val itemDefinition = (arrayDefinition \ "items").as[JsObject]

    ((arrayDefinition \ "minItems").asOpt[Int].map(minItems =>
      assertThat({array(_:JsValue).size}).isGreaterThanOrEqualTo(minItems)
        .otherwise[String]((js:JsValue) => s"Array ${path.toString} has ${array(js).size} items, but a minimum of $minItems is required.")).toList ++
      (arrayDefinition \ "maxItems").asOpt[Int].map(maxItems =>
        assertThat({array(_:JsValue).size}).isLessThanOrEqualTo(maxItems)
          .otherwise[String](s"Array ${path.toString} exceeds maximum items allowed that is $maxItems.")).toList ++
      (arrayDefinition \ "uniqueItems").asOpt[Boolean].map(_ =>
        assertThat(array).containsNoDuplicates
          .otherwise[String](s"Array ${path.toString} should contain no duplicates.")))
      .reduceOption[ComposableAssertionExp[JsValue,JsValue,JsValue]](_ ifTrue _).getOrElse(SuccessfulAssertionExp())
      .ifTrue {
        (context: JsValue) =>
          array(context)
            .zipWithIndex
            .foldLeft[AssertionResultBehaviour[JsValue]](AssertionSuccessfulResult(context)) {
              case (assertionResult,(_, index)) =>
                assertionResult
                  .and(DelegatingJsValidationBuilder.build(path.add(index), itemDefinition)
                    .evaluate(context)) }
      }
  }
}
