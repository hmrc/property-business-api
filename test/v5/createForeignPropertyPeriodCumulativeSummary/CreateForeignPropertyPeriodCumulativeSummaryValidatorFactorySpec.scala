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

package v5.createForeignPropertyPeriodCumulativeSummary

import api.controllers.validators.Validator
import api.models.domain.TaxYear
import api.models.utils.JsonErrorValidators
import config.MockAppConfig
import play.api.libs.json._
import support.UnitSpec
import v5.createForeignPropertyPeriodCumulativeSummary.def1.Def1_CreateForeignPropertyPeriodCumulativeSummaryValidator
import v5.createForeignPropertyPeriodCumulativeSummary.model.request.CreateForeignPropertyPeriodCumulativeSummaryRequestData

class CreateForeignPropertyPeriodCumulativeSummaryValidatorFactorySpec extends UnitSpec with MockAppConfig with JsonErrorValidators {

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2025-26"

  private val validFromDate = "2025-03-29"
  private val validToDate   = "2026-03-29"

  private def validBody(startDate: String, endDate: String) = Json.parse(s"""
       |{
       |   "fromDate":"$startDate",
       |   "toDate":"$endDate",
       |   "foreignFhlEea":{},
       |   "foreignProperty": {}
       |}
       |""".stripMargin)

  private val validatorFactory = new CreateForeignPropertyPeriodCumulativeSummaryValidatorFactory(mockAppConfig)

  "validator()" when {
    "given a valid tax year" should {
      "return the Validator for schema definition 1" in {

        MockedAppConfig.minimumTaxV2Foreign.returns(TaxYear.starting(2021)).anyNumberOfTimes()
        val requestBody = validBody(validFromDate, validToDate)
        val result: Validator[CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTaxYear, requestBody)

        result shouldBe a[Def1_CreateForeignPropertyPeriodCumulativeSummaryValidator]
      }
    }
  }

}
