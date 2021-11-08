/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v2.controllers.requestParsers.validators.validations

import play.api.libs.json.{ JsObject, Json, OFormat }
import support.UnitSpec
import v2.models.errors.RuleIncorrectOrEmptyBodyError
import v2.models.utils.JsonErrorValidators

class JsonFormatValidationSpec extends UnitSpec with JsonErrorValidators {

  case class TestDataObject(field1: String, field2: String)
  case class TestDataWrapper(arrayField: Seq[TestDataObject])

  implicit val testDataObjectFormat: OFormat[TestDataObject]   = Json.format[TestDataObject]
  implicit val testDataWrapperFormat: OFormat[TestDataWrapper] = Json.format[TestDataWrapper]

  "validate" should {
    "return no errors" when {
      "when a valid JSON object with all the necessary fields is supplied" in {

        val validJson = Json.parse("""{ "field1" : "Something", "field2" : "SomethingElse" }""")

        val validationResult = JsonFormatValidation.validate[TestDataObject](validJson)
        validationResult shouldBe empty
      }
    }

    "return an error " when {
      "required field is missing" in {
        val json = Json.parse("""{ "field1" : "Something" }""")

        val validationResult = JsonFormatValidation.validate[TestDataObject](json)
        validationResult shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/field2"))))
      }

      "required field is missing in array object" in {
        val json = Json.parse("""{ "arrayField" : [{ "field1" : "Something" }]}""")

        val validationResult = JsonFormatValidation.validate[TestDataWrapper](json)
        validationResult shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/arrayField/0/field2"))))
      }

      "required field is missing in multiple array objects" in {
        val json = Json.parse("""{ "arrayField" : [{ "field1" : "Something" }, { "field1" : "Something" }]}""")

        val validationResult = JsonFormatValidation.validate[TestDataWrapper](json)
        validationResult shouldBe List(
          RuleIncorrectOrEmptyBodyError.copy(
            paths = Some(
              Seq(
                "/arrayField/0/field2",
                "/arrayField/1/field2"
              ))))
      }

      "empty body is submitted" in {
        val json = Json.parse("""{}""")

        val validationResult = JsonFormatValidation.validate[TestDataObject](json)
        validationResult shouldBe List(RuleIncorrectOrEmptyBodyError)
      }

      "a non-empty body is supplied without any expected fields" in {
        val json = Json.parse("""{"field": "value"}""")

        val validationResult = JsonFormatValidation.validate[TestDataObject](json)
        validationResult shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/field1", "/field2"))))
      }

      "a field is supplied with the wrong data type" in {
        val json = Json.parse("""{"field1": true, "field2": "value"}""")

        val validationResult = JsonFormatValidation.validate[TestDataObject](json)
        validationResult shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/field1"))))
      }
    }
  }

  "validateEmptyFields" when {
    "empty body" must {
      "return error" in {
        JsonFormatValidation.validatedNestedEmpty(JsObject.empty) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
    }

    "all arrays and objects are empty" must {
      "return successfully" in {
        JsonFormatValidation.validatedNestedEmpty(Json.parse("""{
              | "obj1": {"a": 1},
              | "arr1": [{"b": 1}]
              |}""".stripMargin)) shouldBe Nil
      }
    }

    "has empty array" must {
      "return an error with the path" in {
        JsonFormatValidation.validatedNestedEmpty(Json.parse("""{
              | "obj1": {"a": 1},
              | "arr1": []
              |}""".stripMargin)) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/arr1"))))
      }
    }

    "has empty array nested" must {
      "return an error with the path" in {
        JsonFormatValidation.validatedNestedEmpty(Json.parse("""{
              | "obj1": {"arr1": []}
              |}""".stripMargin)) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/obj1/arr1"))))
      }
    }

    "has empty object" must {
      "return an error with the path" in {
        JsonFormatValidation.validatedNestedEmpty(Json.parse("""{
              | "obj1": {},
              | "arr1": [{"a": 1}]
              |}""".stripMargin)) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/obj1"))))
      }
    }

    "has empty object nested" must {
      "return an error with the path" in {
        JsonFormatValidation.validatedNestedEmpty(Json.parse("""{
              | "obj1": { "obj2": {}},
              | "arr1": [{"a": 1}]
              |}""".stripMargin)) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/obj1/obj2"))))
      }
    }

    "has empty object inside arrays" must {
      "return an error with the path" in {
        JsonFormatValidation.validatedNestedEmpty(Json.parse("""{
              | "arr1": [{"obj1": {}}, {"obj2": {}}]
              |}""".stripMargin)) shouldBe List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/arr1/0/obj1", "/arr1/1/obj2"))))
      }
    }

    "has multiple empty objects" must {
      "return an error with the paths for all of them" in {
        JsonFormatValidation.validatedNestedEmpty(Json.parse("""{
              | "obj1": { "obj2": {}},
              | "arr1": [],
              | "arr2": [{ "obj3": {}}],
              | "arr3": [{}],
              | "obj4": {}
              |}""".stripMargin)) shouldBe List(
          RuleIncorrectOrEmptyBodyError
            .copy(paths = Some(Seq("/obj1/obj2", "/arr1", "/arr2/0/obj3", "/arr3/0", "/obj4"))))
      }
    }
  }
}
