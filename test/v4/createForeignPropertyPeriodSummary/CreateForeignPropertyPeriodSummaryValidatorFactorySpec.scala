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

package v4.createForeignPropertyPeriodSummary

import api.controllers.validators.Validator
import api.models.domain.TaxYear
import api.models.utils.JsonErrorValidators
import mocks.MockAppConfig
import play.api.libs.json._
import support.UnitSpec
import v4.createForeignPropertyPeriodSummary.def1.Def1_CreateForeignPropertyPeriodSummaryValidator
import v4.createForeignPropertyPeriodSummary.def2.Def2_CreateForeignPropertyPeriodSummaryValidator
import v4.createForeignPropertyPeriodSummary.model.request.CreateForeignPropertyPeriodSummaryRequestData

class CreateForeignPropertyPeriodSummaryValidatorFactorySpec extends UnitSpec with MockAppConfig with JsonErrorValidators {

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2023-24"
  private val validTyTaxYear  = "2024-25"

  private val validFromDate = "2020-03-29"
  private val validToDate   = "2021-03-29"

  private val validFromDateTy = "2024-04-06"
  private val validToDateTy   = "2024-07-05"

  private def validBody(startDate: String, endDate: String) = Json.parse(s"""
       |{
       |   "fromDate":"$startDate",
       |   "toDate":"$endDate",
       |   "foreignFhlEea":{},
       |   "foreignNonFhlProperty": {}
       |}
       |""".stripMargin)

  private val validatorFactory = new CreateForeignPropertyPeriodSummaryValidatorFactory(mockAppConfig)

  "validator()" when {
    "given a valid tax year" should {
      "return the Validator for schema definition 1" in {

        MockedAppConfig.minimumTaxV2Foreign.returns(TaxYear.starting(2021)).anyNumberOfTimes()
        val requestBody = validBody(validFromDate, validToDate)
        val result: Validator[CreateForeignPropertyPeriodSummaryRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTaxYear, requestBody)

        result shouldBe a[Def1_CreateForeignPropertyPeriodSummaryValidator]
      }
      "return the Validator for schema definition 2" in {

        val requestBody = validBody(validFromDateTy, validToDateTy)
        val result: Validator[CreateForeignPropertyPeriodSummaryRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTyTaxYear, requestBody)

        result shouldBe a[Def2_CreateForeignPropertyPeriodSummaryValidator]
      }
    }
  }

}
