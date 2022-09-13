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
import v2.models.errors.{ NinoFormatError, PeriodIdFormatError, RuleIncorrectOrEmptyBodyError, ValueFormatError }
import v2.models.utils.JsonErrorValidators

class AmendHistoricNonFhlUkPiePeriodSummaryValidatorSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  private val validNino     = "AA123456A"
  private val validPeriodId = "2017-04-06_2017-07-04"

  MockAppConfig.minimumTaxHistoric returns 2017
  MockAppConfig.maximumTaxHistoric returns 2021

  val validator = new AmendHistoricNonFhlUkPiePeriodSummaryValidator(mockAppConfig)

  val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "income":{
      |      "periodAmount": 5000.99,
      |      "premiumsOfLeaseGrant": 4999.99,
      |      "reversePremiums": 4998.99,
      |      "otherIncome": 4997.99,
      |      "taxDeducted": 4996.99,
      |      "rentARoom":{
      |         "rentsReceived": 4995.99
      |       }
      |   },
      |   "expenses":{
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 4999.99,
      |      "financialCosts": 4998.99,
      |      "professionalFees": 4997.99,
      |      "costOfServices": 4996.99,
      |      "other": 4995.99,
      |      "travelCosts": 4994.99,
      |      "residentialFinancialCostsCarriedForward": 4993.99,
      |      "residentialFinancialCost": 4992.99,
      |      "rentARoom":{
      |         "amountClaimed": 4991.99
      |       }
      |   }
      |}
      |""".stripMargin
  )

  val validConsolidatedRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "income":{
      |      "periodAmount": 5000.99,
      |      "premiumsOfLeaseGrant": 4999.99,
      |      "reversePremiums": 4998.99,
      |      "otherIncome": 4997.99,
      |      "taxDeducted": 4996.99,
      |      "rentARoom":{
      |         "rentsReceived": 4995.99
      |       }
      |   },
      |   "expenses":{
      |      "consolidatedExpenses": 5000.99
      |    }
      |}
      |""".stripMargin
  )

  val requestBodyJsonWithInvalidAmounts: JsValue = Json.parse(
    """
      |{
      |   "income":{
      |      "periodAmount": 5000.99,
      |      "premiumsOfLeaseGrant": 4999.99,
      |      "reversePremiums": 4998.99,
      |      "otherIncome": 4997.99,
      |      "taxDeducted": -4996.99,
      |      "rentARoom":{
      |         "rentsReceived": 9999999.99
      |       }
      |   },
      |   "expenses":{
      |      "consolidatedExpenses": 5000.99
      |    }
      |}
      |""".stripMargin
  )

  val requestBodyJsonWithEmptySubObjects: JsValue = Json.parse(
    """
      |{
      |   "income":{},
      |   "expenses":{}
      |}
      |""".stripMargin
  )

  val requestBodyWithEmptyRentARoom: JsValue = Json.parse(
    """
      |{
      |   "income":{
      |      "periodAmount": 5000.99,
      |      "premiumsOfLeaseGrant": 4999.99,
      |      "reversePremiums": 4998.99,
      |      "otherIncome": 4997.99,
      |      "taxDeducted": 4996.99,
      |      "rentARoom":{}
      |   },
      |   "expenses":{
      |      "consolidatedExpenses": 5000.99
      |    }
      |}
      |""".stripMargin
  )

  "The validator" should {
    "return no errors" when {
      "given a valid request" in {
        val result = validator.validate(AmendHistoricNonFhlUkPiePeriodSummaryRawData(validNino, validPeriodId, validRequestBodyJson))
        result shouldBe empty
      }
      "given a valid request with consolidated expenses" in {
        val result = validator.validate(AmendHistoricNonFhlUkPiePeriodSummaryRawData(validNino, validPeriodId, validConsolidatedRequestBodyJson))
        result shouldBe empty
      }
    }
    "return multiple errors" when {
      "a multiple fields failed validation" in {
        val result = validator.validate(AmendHistoricNonFhlUkPiePeriodSummaryRawData("AA1234A", "20123", validRequestBodyJson))
        result shouldBe List(NinoFormatError, PeriodIdFormatError)
      }
    }
    "return ValueFormatErrors grouped into one error object with an array of paths" when {
      "given data with multiple invalid numeric amounts" in {
        val expected =
          ValueFormatError.copy(paths = Some(List("/income/taxDeducted", "/income/rentARoom/rentsReceived")))
        val result =
          validator.validate(AmendHistoricNonFhlUkPiePeriodSummaryRawData(validNino, validPeriodId, requestBodyJsonWithInvalidAmounts))
        result should contain only expected
      }
    }
    "return RuleIncorrectOrEmptyBodyError" when {
      "given an empty body" in {
        val result = validator.validate(AmendHistoricNonFhlUkPiePeriodSummaryRawData(validNino, validPeriodId, JsObject.empty))
        result should contain only RuleIncorrectOrEmptyBodyError
      }
      "given empty income and expenses sub-objects" in {
        val expected = RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/income", "/expenses")))
        val result =
          validator.validate(AmendHistoricNonFhlUkPiePeriodSummaryRawData(validNino, validPeriodId, requestBodyJsonWithEmptySubObjects))
        result should contain only expected
      }
      "given an empty rentARoom sub-object" in {
        val expected = RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/income/rentARoom")))
        val result =
          validator.validate(AmendHistoricNonFhlUkPiePeriodSummaryRawData(validNino, validPeriodId, requestBodyWithEmptyRentARoom))
        result should contain only expected
      }
    }
    "return PeriodIdFormatError error" when {
      "given a periodId with invalid format" in {
        val result =
          validator.validate(AmendHistoricNonFhlUkPiePeriodSummaryRawData(validNino, periodId = "20A7-04-06_2017-07-04", validRequestBodyJson))
        result should contain only PeriodIdFormatError
      }
      "given a periodId with a non-historic year" in {
        val result =
          validator.validate(AmendHistoricNonFhlUkPiePeriodSummaryRawData(validNino, periodId = "2012-04-06_2012-07-04", validRequestBodyJson))
        result should contain only PeriodIdFormatError
      }
      "given a periodId with the toDate before fromDate" in {
        val result =
          validator.validate(AmendHistoricNonFhlUkPiePeriodSummaryRawData(validNino, periodId = "2019-07-04_2019-04-06", validRequestBodyJson))
        result should contain only PeriodIdFormatError
      }
    }
    "return only the path-param errors" when {
      "given a request with both invalid path params and an invalid body" in {
        val result =
          validator.validate(AmendHistoricNonFhlUkPiePeriodSummaryRawData("BAD-NINO", validPeriodId, requestBodyJsonWithInvalidAmounts))
        result should contain only NinoFormatError
      }
    }
  }
}
