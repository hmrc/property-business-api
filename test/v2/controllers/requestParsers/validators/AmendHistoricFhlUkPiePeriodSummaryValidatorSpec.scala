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
import v2.models.request.amendHistoricFhlUkPiePeriodSummary.AmendHistoricFhlUkPiePeriodSummaryRawData
import v2.models.utils.JsonErrorValidators

class AmendHistoricFhlUkPiePeriodSummaryValidatorSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  private val validNino     = "AA123456A"
  private val validPeriodId = "2017-04-06_2017-07-04"

  MockAppConfig.minimumTaxHistoric returns 2017
  MockAppConfig.maximumTaxHistoric returns 2021

  val validator = new AmendHistoricFhlUkPiePeriodSummaryValidator(mockAppConfig)

  val validRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "income":{
      |      "periodAmount":1123.45,
      |      "taxDeducted":2134.53,
      |      "rentARoom":{
      |         "rentsReceived":5167.56
      |       }
      |   },
      |   "expenses":{
      |      "premisesRunningCosts":5167.53,
      |      "repairsAndMaintenance":424.65,
      |      "financialCosts":853.56,
      |      "professionalFees":835.78,
      |      "costOfServices":978.34,
      |      "other":382.34,
      |      "travelCosts":145.56,
      |      "rentARoom":{
      |         "amountClaimed":945.9
      |       }
      |   }
      |}
      |""".stripMargin
  )

  val validConsolidatedRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "income":{
      |      "periodAmount":1123.45,
      |      "taxDeducted":2134.53,
      |      "rentARoom":{
      |         "rentsReceived":5167.56
      |       }
      |   },
      |   "expenses":{
      |      "consolidatedExpenses":135.78
      |    }
      |}
      |""".stripMargin
  )

  val requestBodyJsonWithInvalidAmounts: JsValue = Json.parse(
    """
      |{
      |   "income":{
      |      "periodAmount":1123.45,
      |      "taxDeducted":-1.00,
      |      "rentARoom":{
      |         "rentsReceived":999999999990.99
      |       }
      |   },
      |   "expenses":{
      |      "consolidatedExpenses":135.78
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
      |      "periodAmount":1123.45,
      |      "taxDeducted":2134.53,
      |      "rentARoom":{}
      |   },
      |   "expenses":{
      |      "consolidatedExpenses":135.78
      |    }
      |}
      |""".stripMargin
  )

  "The validator" should {
    "return no errors" when {
      "given a valid request" in {
        val result = validator.validate(AmendHistoricFhlUkPiePeriodSummaryRawData(validNino, validPeriodId, validRequestBodyJson))
        result shouldBe empty
      }
      "given a valid request with consolidated expenses" in {
        val result = validator.validate(AmendHistoricFhlUkPiePeriodSummaryRawData(validNino, validPeriodId, validConsolidatedRequestBodyJson))
        result shouldBe empty
      }
    }
    "return multiple errors" when {
      "a multiple fields failed validation" in {
        val result = validator.validate(AmendHistoricFhlUkPiePeriodSummaryRawData("AA1234A", "20123", validRequestBodyJson))
        result shouldBe List(NinoFormatError, PeriodIdFormatError)
      }
    }
    "return ValueFormatErrors grouped into one error object with an array of paths" when {
      "given data with multiple invalid numeric amounts" in {
        val expected =
          ValueFormatError.copy(paths = Some(List("/income/taxDeducted", "/income/rentARoom/rentsReceived")))
        val result =
          validator.validate(AmendHistoricFhlUkPiePeriodSummaryRawData(validNino, validPeriodId, requestBodyJsonWithInvalidAmounts))
        result should contain only expected
      }
    }
    "return RuleIncorrectOrEmptyBodyError" when {
      "given an empty body" in {
        val result = validator.validate(AmendHistoricFhlUkPiePeriodSummaryRawData(validNino, validPeriodId, JsObject.empty))
        result should contain only RuleIncorrectOrEmptyBodyError
      }
      "given empty income and expenses sub-objects" in {
        val expected = RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/income", "/expenses")))
        val result =
          validator.validate(AmendHistoricFhlUkPiePeriodSummaryRawData(validNino, validPeriodId, requestBodyJsonWithEmptySubObjects))
        result should contain only expected
      }
      "given an empty rentARoom sub-object" in {
        val expected = RuleIncorrectOrEmptyBodyError.copy(paths = Some(List("/income/rentARoom")))
        val result =
          validator.validate(AmendHistoricFhlUkPiePeriodSummaryRawData(validNino, validPeriodId, requestBodyWithEmptyRentARoom))
        result should contain only expected
      }
    }
    "return PeriodIdFormatError error" when {
      "given a periodId with invalid format" in {
        val result =
          validator.validate(AmendHistoricFhlUkPiePeriodSummaryRawData(validNino, periodId = "20A7-04-06_2017-07-04", validRequestBodyJson))
        result should contain only PeriodIdFormatError
      }
      "given a periodId with a non-historic year" in {
        val result =
          validator.validate(AmendHistoricFhlUkPiePeriodSummaryRawData(validNino, periodId = "2012-04-06_2012-07-04", validRequestBodyJson))
        result should contain only PeriodIdFormatError
      }
      "given a periodId with the toDate before fromDate" in {
        val result =
          validator.validate(AmendHistoricFhlUkPiePeriodSummaryRawData(validNino, periodId = "2019-07-04_2019-04-06", validRequestBodyJson))
        result should contain only PeriodIdFormatError
      }
    }
    "return only the path-param errors" when {
      "given a request with both invalid path params and an invalid body" in {
        val result =
          validator.validate(AmendHistoricFhlUkPiePeriodSummaryRawData("BAD-NINO", validPeriodId, requestBodyJsonWithInvalidAmounts))
        result should contain only NinoFormatError
      }
    }
  }
}
