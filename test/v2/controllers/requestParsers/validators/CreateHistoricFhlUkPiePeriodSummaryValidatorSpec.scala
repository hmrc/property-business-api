/*
 * Copyright 2023 HM Revenue & Customs
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

package v2.controllers.requestParsers.validators

import api.models.errors._
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import support.UnitSpec
import v2.models.request.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryRawData
import v2.models.utils.JsonErrorValidators

class CreateHistoricFhlUkPiePeriodSummaryValidatorSpec extends UnitSpec with JsonErrorValidators {

  private val validNino = "AA123456A"

  val validator = new CreateHistoricFhlUkPiePeriodSummaryValidator

  private val validRequestBody: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2017-04-06",
      |  "toDate": "2017-07-05",
      |  "income": {
      |    "periodAmount": 100.25,
      |    "taxDeducted": 100.25,
      |    "rentARoom": {
      |      "rentsReceived": 100.25
      |    }
      |  },
      |  "expenses": {
      |    "premisesRunningCosts": 100.25,
      |    "repairsAndMaintenance": 100.25,
      |    "financialCosts": 100.25,
      |    "professionalFees": 100.25,
      |    "costOfServices": 100.25,
      |    "travelCosts": 100.25,
      |    "other": 100.25,
      |    "rentARoom": {
      |      "amountClaimed": 100.25
      |    }
      |  }
      |}
      |""".stripMargin
  )

  private val validRequestBodyConsolidated: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2017-04-06",
      |  "toDate": "2017-07-05",
      |  "income": {
      |    "periodAmount": 100.25,
      |    "taxDeducted": 100.25,
      |    "rentARoom": {
      |      "rentsReceived": 100.25
      |    }
      |  },
      |  "expenses": {
      |    "consolidatedExpenses": 100.25
      |  }
      |}
    """.stripMargin
  )

  private val incompleteRequestBody: JsValue = Json.parse(
    """
      |{
      |  "fromDate-MISSING": "2017-04-06",
      |  "toDate": "2017-07-05",
      |  "income": {
      |    "periodAmount": 100.25,
      |    "taxDeducted": 100.25,
      |    "rentARoom": {
      |      "rentsReceived": 100.25
      |    }
      |  },
      |  "expenses": {
      |    "consolidatedExpenses": 100.25
      |  }
      |}
      """.stripMargin
  )

  private val requestBodyWithInvalidAmounts: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2017-04-06",
      |  "toDate": "2017-07-05",
      |  "income": {
      |    "periodAmount": 9999999999999999999.25,
      |    "taxDeducted": -100.25,
      |    "rentARoom": {
      |      "rentsReceived": 100.25
      |    }
      |  },
      |  "expenses": {
      |    "consolidatedExpenses": -20000.72
      |  }
      |}
    """.stripMargin
  )

  private val requestBodyWithNoSubObjects: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2017-04-06",
      |  "toDate": "2017-07-05"
      |}
    """.stripMargin
  )

  "The validator" should {
    "return no errors" when {
      "given a valid request" in {
        val result = validator.validate(CreateHistoricFhlUkPiePeriodSummaryRawData(validNino, validRequestBody))
        result shouldBe empty
      }

      "given a valid request object with consolidated expenses" in {
        val result = validator.validate(CreateHistoricFhlUkPiePeriodSummaryRawData(validNino, validRequestBodyConsolidated))
        result shouldBe empty
      }
    }

    "return an error" when {
      "a mandatory field is missing" in {
        val expected = RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/fromDate")))
        val result   = validator.validate(CreateHistoricFhlUkPiePeriodSummaryRawData(validNino, incompleteRequestBody))

        result should contain only expected
      }

      "given only a fromDate and toDate" in {
        validator.validate(CreateHistoricFhlUkPiePeriodSummaryRawData(validNino, requestBodyWithNoSubObjects)) should
          contain only RuleIncorrectOrEmptyBodyError
      }
    }

    "return ValueFormatErrors grouped into one error object with an array of paths" when {
      "given data with multiple invalid numeric amounts" in {
        val expected =
          ValueFormatError.copy(paths = Some(List("/income/periodAmount", "/income/taxDeducted", "/expenses/consolidatedExpenses")))

        val result =
          validator.validate(CreateHistoricFhlUkPiePeriodSummaryRawData(validNino, requestBodyWithInvalidAmounts))

        result should contain only expected
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "given an empty body" in {
        val result = validator.validate(CreateHistoricFhlUkPiePeriodSummaryRawData(validNino, JsObject.empty))
        result should contain only RuleIncorrectOrEmptyBodyError
      }
    }

    "return FromDateFormatError error" when {
      "given an invalid fromDate" in {
        val result =
          validator.validate(CreateHistoricFhlUkPiePeriodSummaryRawData(validNino, validRequestBody.update("fromDate", JsString("BAD_DATE"))))
        result should contain only FromDateFormatError
      }
    }

    "return ToDateFormatError error" when {
      "given an invalid toDate" in {
        val result =
          validator.validate(CreateHistoricFhlUkPiePeriodSummaryRawData(validNino, validRequestBody.update("toDate", JsString("BAD_DATE"))))
        result should contain only ToDateFormatError
      }
    }

    "return RuleToDateBeforeFromDateError error" when {
      "given an toDate is before the fromDate" in {
        validator.validate(CreateHistoricFhlUkPiePeriodSummaryRawData(validNino, validRequestBody.update("fromDate", JsString("2099-01-01")))) should
          contain only RuleToDateBeforeFromDateError
      }
    }

    "return only the path-param errors" when {
      "given a request with both invalid path params and an invalid body" in {
        val result =
          validator.validate(CreateHistoricFhlUkPiePeriodSummaryRawData("BAD-NINO", requestBodyWithInvalidAmounts))
        result should contain only NinoFormatError
      }
    }
  }

}
