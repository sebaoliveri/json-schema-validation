# json-schema-validation

Validate JSONs against json schemas

A fairly complete (not total) implementation of [json schema spec](https://json-schema.org/draft/2019-09/json-schema-validation.html)

Can be trustely used in Prod, just check all keywords described in your schema are supported by this Lib.

### Usage

```scala

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
          },
          "required": ["lastName"]
        }
      """)
      
      val person =
      Json.parse(
        """{
           "firstName": "John",
           "age": -1
         }""")

    JsValidation.from(jsonSchema).appliedTo(person).matches {
      case AssertionSuccessfulResult(_) => fail()
      case AssertionFailureResult(errors) => errors should be(
        List(
          "Property Person.lastName is missing",
          "Person.age is smaller than required minimum value of 0"))
    }

```

 

