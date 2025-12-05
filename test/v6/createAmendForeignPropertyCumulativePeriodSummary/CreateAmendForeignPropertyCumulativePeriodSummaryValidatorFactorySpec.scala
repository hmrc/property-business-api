/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.createAmendForeignPropertyCumulativePeriodSummary

import play.api.libs.json.*
import shared.controllers.validators.{AlwaysErrorsValidator, Validator}
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v6.createAmendForeignPropertyCumulativePeriodSummary.def1.Def1_CreateAmendForeignPropertyCumulativePeriodSummaryValidator
import v6.createAmendForeignPropertyCumulativePeriodSummary.def2.Def2_CreateAmendForeignPropertyCumulativePeriodSummaryValidator
import v6.createAmendForeignPropertyCumulativePeriodSummary.model.request.CreateAmendForeignPropertyCumulativePeriodSummaryRequestData

class CreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactorySpec extends UnitSpec with JsonErrorValidators {

  private def validatorFor(taxYear: String): Validator[CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
    new CreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactory().validator(
      nino = "ignoredNino",
      taxYear = taxYear,
      businessId = "ignoredBusinessId",
      body = JsObject.empty)

  "validator" when {
    "given a request corresponding to a Def1 schema" should {
      "return a Def1 Validator" in {
        validatorFor("2025-26") shouldBe a[Def1_CreateAmendForeignPropertyCumulativePeriodSummaryValidator]
      }
    }
    "given a request corresponding to a Def 2 schema" should {
      "return a Def2 Validator" in {
        validatorFor("2026-27") shouldBe a[Def2_CreateAmendForeignPropertyCumulativePeriodSummaryValidator]
      }
    }

    "given a request where no valid schema could be determined" should {
      "return a validator returning the errors" in {
        validatorFor("BAD_TAX_YEAR") shouldBe an[AlwaysErrorsValidator]
      }
    }
  }

}
