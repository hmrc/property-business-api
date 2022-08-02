/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.{ JsObject, JsValue, Json }
import support.UnitSpec
import v2.models.errors.{
  FromDateFormatError,
  NinoFormatError,
  RuleHistoricTaxYearNotSupportedError,
  RuleIncorrectOrEmptyBodyError,
  TaxYearFormatError,
  ToDateFormatError,
  ValueFormatError
}
import v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary.CreateHistoricNonFhlUkPropertyPeriodSummaryRawData

class CreateHistoricNonFhlUkPropertyPeriodSummaryValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino = "AA123456A"

  MockAppConfig.minimumTaxHistoric returns 2017
  MockAppConfig.maximumTaxHistoric returns 2021

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

  val validRequestBodyConsolidated: JsValue = Json.parse(
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

  val incompleteRequestBody: JsValue = Json.parse(
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

  val requestBodyWithInvalidAmounts: JsValue = Json.parse(
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

  val requestBodyWithNoSubObjects: JsValue = Json.parse(
    """
      |{
      |    "fromDate": "2019-03-11",
      |    "toDate": "2020-04-23"
      |}
    """.stripMargin
  )

  val requestBodyWithInvalidFromDateFormat: JsValue = Json.parse(
    """
      |{
      |    "fromDate": "2019-A3-11",
      |    "toDate": "2020-04-23"
      |}
    """.stripMargin
  )

  val requestBodyWithInvalidToDateFormat: JsValue = Json.parse(
    """
      |{
      |    "fromDate": "2019-03-11",
      |    "toDate": "2020-A4-23"
      |}
    """.stripMargin
  )

  val requestBodyWithInvalidFromDateYear: JsValue = Json.parse(
    """
      |{
      |    "fromDate": "2016-03-11",
      |    "toDate": "2017-04-23"
      |}
    """.stripMargin
  )

  val requestBodyWithInvalidToDateYear: JsValue = Json.parse(
    """
      |{
      |    "fromDate": "2022-03-11",
      |    "toDate": "2023-04-23"
      |}
    """.stripMargin
  )

  "The validator" should {
    "return no errors" when {
      "given a valid request" in {
        val result = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, validRequestBody))
        result shouldBe empty
      }

      "given a valid request object that contains expenses in consolidated format" in {
        val result = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, validRequestBodyConsolidated))
        result shouldBe empty
      }

      "given only a fromDate and toDate" in {
        val result =
          validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, requestBodyWithNoSubObjects))

        result shouldBe empty
      }
    }

    "return an error" when {
      "a mandatory field is missing" in {
        val expected = RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/fromDate")))
        val result   = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, incompleteRequestBody))

        result should contain only (expected)
      }
    }

    "return ValueFormatErrors grouped into one error object with an array of paths" when {
      "given data with multiple invalid numeric amounts" in {
        val expected =
          ValueFormatError.copy(paths = Some(List("/income/periodAmount", "/income/premiumsOfLeaseGrant", "/expenses/consolidatedExpenses")))

        val result =
          validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, requestBodyWithInvalidAmounts))

        result should contain only (expected)
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "given an empty body" in {
        val result = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, JsObject.empty))
        result should contain only (RuleIncorrectOrEmptyBodyError)
      }
    }

    "return FromDateFormatError error" when {
      "given an invalid fromDate" in {
        val result = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, requestBodyWithInvalidFromDateFormat))
        result should contain only (FromDateFormatError, TaxYearFormatError)
      }
    }

    "return ToDateFormatError error" when {
      "given an invalid toDate" in {
        val result = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, requestBodyWithInvalidToDateFormat))
        result should contain only (ToDateFormatError, TaxYearFormatError)
      }
    }

    "return RuleHistoricTaxYearNotSupportedError error" when {
      "given an invalid fromDate" in {
        val result = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, requestBodyWithInvalidFromDateYear))
        result should contain only (RuleHistoricTaxYearNotSupportedError)
      }
    }

    "return RuleHistoricTaxYearNotSupportedError error" when {
      "given an invalid toDate" in {
        val result = validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(validNino, requestBodyWithInvalidToDateYear))
        result should contain only (RuleHistoricTaxYearNotSupportedError)
      }
    }

    "return only the path-param errors" when {
      "given a request with both invalid path params and an invalid body" in {
        val result =
          validator.validate(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData("BAD-NINO", requestBodyWithInvalidAmounts))
        result should contain only (NinoFormatError)
      }
    }
  }
}
