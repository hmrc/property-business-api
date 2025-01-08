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

package v5.createAmendForeignPropertyCumulativePeriodSummary

import shared.controllers.validators.Validator
import cats.data.Validated.{Invalid, Valid}
import play.api.libs.json.JsValue
import v5.createAmendForeignPropertyCumulativePeriodSummary.def1.Def1_CreateAmendForeignPropertyCumulativePeriodSummaryValidator
import v5.createAmendForeignPropertyCumulativePeriodSummary.model.request.CreateAmendForeignPropertyCumulativePeriodSummaryRequestData

import javax.inject.Singleton

@Singleton
class CreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactory {

  def validator(nino: String,
                businessId: String,
                taxYear: String,
                body: JsValue): Validator[CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
    CreateAmendForeignPropertyCumulativePeriodSummarySchema.schemaFor(taxYear) match {
      case Valid(CreateAmendForeignPropertyCumulativePeriodSummarySchema.Def1) =>
        new Def1_CreateAmendForeignPropertyCumulativePeriodSummaryValidator(
          nino,
          businessId,
          taxYear,
          body
        )
      case Invalid(errors) => Validator.returningErrors(errors)
    }

}
