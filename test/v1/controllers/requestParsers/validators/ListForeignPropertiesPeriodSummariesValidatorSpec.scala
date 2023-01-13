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

package v1.controllers.requestParsers.validators

import support.UnitSpec
import v1.models.errors._
import v1.models.request.listForeignPropertiesPeriodSummaries.ListForeignPropertiesPeriodSummariesRawData

class ListForeignPropertiesPeriodSummariesValidatorSpec extends UnitSpec {

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validFromDate   = "2020-06-06"
  private val validToDate     = "2020-08-06"

  private val validator = new ListForeignPropertiesPeriodSummariesValidator

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(ListForeignPropertiesPeriodSummariesRawData(validNino, validBusinessId, Some(validFromDate), Some(validToDate))) shouldBe Nil
      }
      "a valid request is supplied without dates" in {
        validator.validate(ListForeignPropertiesPeriodSummariesRawData(validNino, validBusinessId, None, None)) shouldBe Nil
      }
    }
    "return a path parameter format error" when {
      "an invalid nino is supplied" in {
        validator.validate(ListForeignPropertiesPeriodSummariesRawData("Walrus", validBusinessId, Some(validFromDate), Some(validToDate))) shouldBe List(
          NinoFormatError)
      }
      "an invalid businessId is supplied" in {
        validator.validate(ListForeignPropertiesPeriodSummariesRawData(validNino, "Beans", Some(validFromDate), Some(validToDate))) shouldBe List(
          BusinessIdFormatError)
      }
      "an invalid fromDate is supplied" in {
        validator.validate(ListForeignPropertiesPeriodSummariesRawData(validNino, validBusinessId, Some("20202-202-202"), Some(validToDate))) shouldBe List(
          FromDateFormatError)
      }
      "an invalid toDate is supplied" in {
        validator.validate(ListForeignPropertiesPeriodSummariesRawData(validNino, validBusinessId, Some(validFromDate), Some("20202-202-202"))) shouldBe List(
          ToDateFormatError)
      }
      "toDate is before fromDate" in {
        validator.validate(ListForeignPropertiesPeriodSummariesRawData(validNino, validBusinessId, Some("2020-08-06"), Some("2020-06-06"))) shouldBe List(
          RuleToDateBeforeFromDateError)
      }
      "only fromDate is provided" in {
        validator.validate(ListForeignPropertiesPeriodSummariesRawData(validNino, validBusinessId, Some("2020-08-06"), None)) shouldBe List(
          MissingToDateError)
      }
      "only toDate is provided" in {
        validator.validate(ListForeignPropertiesPeriodSummariesRawData(validNino, validBusinessId, None, Some("2020-08-06"))) shouldBe List(
          MissingFromDateError)
      }
      "multiple format errors are made" in {
        validator.validate(ListForeignPropertiesPeriodSummariesRawData("Walrus", "Beans", Some(validFromDate), Some(validToDate))) shouldBe List(
          NinoFormatError,
          BusinessIdFormatError)
      }
    }
  }
}
