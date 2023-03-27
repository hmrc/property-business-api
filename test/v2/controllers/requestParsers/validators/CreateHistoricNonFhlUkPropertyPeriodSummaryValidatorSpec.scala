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

import mocks.MockAppConfig
import play.api.libs.json.{ JsObject, JsString, JsValue, Json }
import support.UnitSpec
import api.models.errors._
import v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary.CreateHistoricNonFhlUkPropertyPeriodSummaryRawData
import v2.models.utils.JsonErrorValidators

class CreateHistoricNonFhlUkPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockAppConfig with JsonErrorValidators {

  private val validNino = "AA123456A"

  val validator = new CreateHistoricNonFhlUkPropertyPeriodSummaryValidator(mockAppConfig)

  private val validRequestBody: JsValue = Json.parse(
    """
      |{
      | "fromDate": "2017-03-11",
      | "toDate": "2018-03-11",
      |   "income": {
      |     "periodAmount": 123.45,
      |     "premiumsOfLeaseGrant": 2355.45,
      |     "reversePremiums": 454.56,
      |     "otherIncome": 567.89,
      |     "taxDeducted": 234.53,
      |     "rentARoom": {
      |       "rentsReceived": 567.56
      |     }
      |   },
      |  "expenses": {
      |    "premisesRunningCosts": 567.53,
      |    "repairsAndMaintenance": 324.65,
      |    "financialCosts": 453.56,
      |    "professionalFees": 535.78,
      |    "costOfServices": 678.34,
      |    "other": 682.34,
      |    "travelCosts": 645.56,
      |    "residentialFinancialCostsCarriedForward": 672.34,
      |    "residentialFinancialCost": 1000.45,
      |    "rentARoom": {
      |      "amountClaimed": 545.9
      |    }
      |  }
      |}
      |""".stripMargin
  )

  private val validRequestBodyConsolidated: JsValue = Json.parse(
    """
      |{
      |    "fromDate": "2019-03-11",
      |    "toDate": "2020-04-23",
      |    "income": {
      |        "periodAmount": 123.45,
      |        "premiumsOfLeaseGrant": 2355.45,
      |        "reversePremiums": 454.56,
      |        "otherIncome": 567.89,
      |        "taxDeducted": 234.53,
      |        "rentARoom": {
      |           "rentsReceived": 567.56
      |         }
      |        },
      |       "expenses":{
      |          "consolidatedExpenses": 235.78
      |     }
      |}
    """.stripMargin
  )

  private val incompleteRequestBody: JsValue = Json.parse(
    """
        |{
        |    "fromDate-MISSING-BECAUSE-INCORRECT-SPELLING": "2019-03-11",
        |    "toDate": "2020-04-23",
        |    "income": {
        |        "periodAmount": 123.45,
        |        "premiumsOfLeaseGrant": 2355.45,
        |        "reversePremiums": 454.56,
        |        "otherIncome": 567.89,
        |        "taxDeducted": 234.53,
        |        "rentARoom": {
        |           "rentsReceived": 567.56
        |         }
        |        },
        |       "expenses":{
        |          "consolidatedExpenses": 235.78
        |     }
        |}
      """.stripMargin
  )

  private val requestBodyWithInvalidAmounts: JsValue = Json.parse(
    """
      |{
      |    "fromDate": "2019-03-11",
      |    "toDate": "2020-04-23",
      |    "income": {
      |        "periodAmount": -1.00,
      |        "premiumsOfLeaseGrant": 999999999990.99,
      |        "reversePremiums": 454.56,
      |        "otherIncome": 567.89,
      |        "taxDeducted": 234.53,
      |        "rentARoom": {
      |           "rentsReceived": 567.56
      |         }
      |        },
      |       "expenses":{
      |          "consolidatedExpenses": 235.781
      |     }
      |}
    """.stripMargin
  )

  private val requestBodyWithNoSubObjects: JsValue = Json.parse(
    """
      |{
      |    "fromDate": "2019-03-11",
      |    "toDate": "2020-04-23"
      |}
    """.stripMargin
  )

  private val requestBodyWithToDateEarlierThanFromDate: JsValue = Json.parse(
    """
      |{
      |    "fromDate": "2020-04-23",
      |    "toDate": "2019-03-11",
      |    "expenses":{
      |       "consolidatedExpenses": 1
      |    }
      |}
    """.stripMargin
  )

  "The validator" should {
    "return no errors" when {
      "given a valid request" in {
        val result = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, validRequestBody))
        result shouldBe empty
      }

      "given a valid request object with consolidated expenses" in {
        val result = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, validRequestBodyConsolidated))
        result shouldBe empty
      }
    }

    "return an error" when {
      "a mandatory field is missing" in {
        val expected = RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/fromDate")))
        val result   = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, incompleteRequestBody))

        result should contain only expected
      }

      "given only a fromDate and toDate" in {
        val result = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, requestBodyWithNoSubObjects))
        result should contain only RuleIncorrectOrEmptyBodyError
      }
    }

    "return ValueFormatErrors grouped into one error object with an array of paths" when {
      "given data with multiple invalid numeric amounts" in {
        val expected =
          ValueFormatError.copy(paths = Some(List("/income/periodAmount", "/income/premiumsOfLeaseGrant", "/expenses/consolidatedExpenses")))
        val result =
          validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, requestBodyWithInvalidAmounts))

        result should contain only expected
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "given an empty body" in {
        val result = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, JsObject.empty))
        result should contain only RuleIncorrectOrEmptyBodyError
      }
    }

    "return FromDateFormatError error" when {
      "given an invalid fromDate" in {
        val result =
          validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, validRequestBody.update("fromDate", JsString("BAD_DATE"))))
        result should contain only FromDateFormatError
      }
    }

    "return ToDateFormatError error" when {
      "given an invalid toDate" in {
        val result =
          validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, validRequestBody.update("toDate", JsString("BAD_DATE"))))
        result should contain only ToDateFormatError
      }
    }

    "return toDateBeforeFromDateError error" when {
      "given a toDate that is earlier than the fromDate" in {
        val result = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, requestBodyWithToDateEarlierThanFromDate))
        result should contain only RuleToDateBeforeFromDateError
      }
    }

    "return only the path-param errors" when {
      "given a request with both invalid path params and an invalid body" in {
        val result =
          validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData("BAD-NINO", requestBodyWithInvalidAmounts))
        result should contain only NinoFormatError
      }
    }
  }
}
