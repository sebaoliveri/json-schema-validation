package jsonschemavalidation

import org.nulluncertainty.expression.{AssertionFailureResult, AssertionSuccessfulResult}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json

class JsValidationTest extends FlatSpec with Matchers {

  val jsonSchema =
    Json.parse(
      """
        {
          "$id": "https://example.com/person.schema.json",
          "$schema": "http://json-schema.org/draft-07/schema#",
          "title": "Person",
          "type": "object",
          "properties": {
            "firstName": {
              "type": "string",
              "description": "The person's first name."
            },
            "lastName": {
              "type": "string",
              "description": "The person's last name."
            },
            "age": {
              "description": "Age in years which must be equal to or greater than zero.",
              "type": "integer",
              "minimum": 0
            }
          }
        }
      """)

  it should "validate json" in {
    val person =
      Json.parse(
        """{
           "firstName": "John",
           "lastName": "Doe",
           "age": "-1"
         }""")

    JsValidation.from(jsonSchema).appliedTo(person).matches {
      case AssertionSuccessfulResult(_) => fail()
      case AssertionFailureResult(firstError :: _) => firstError should be("Person.age is expected to be int")
    }
  }

}
