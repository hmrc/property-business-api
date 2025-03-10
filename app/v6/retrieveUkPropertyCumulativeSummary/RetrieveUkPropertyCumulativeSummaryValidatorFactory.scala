/*
 * Copyright 2024 HM Revenue & Customs
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

package v6.retrieveUkPropertyCumulativeSummary

import shared.controllers.validators.Validator
import cats.data.Validated.{Invalid, Valid}
import v6.retrieveUkPropertyCumulativeSummary.def1.Def1_RetrieveUkPropertyCumulativeSummaryValidator
import v6.retrieveUkPropertyCumulativeSummary.model.request.RetrieveUkPropertyCumulativeSummaryRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveUkPropertyCumulativeSummaryValidatorFactory @Inject() {

  def validator(nino: String, businessId: String, taxYear: String): Validator[RetrieveUkPropertyCumulativeSummaryRequestData] =
    RetrieveUkPropertyCumulativeSummarySchema.schemaFor(taxYear) match {
      case Valid(RetrieveUkPropertyCumulativeSummarySchema.Def1) => new Def1_RetrieveUkPropertyCumulativeSummaryValidator(nino, businessId, taxYear)
      case Invalid(errors)                                       => Validator.returningErrors(errors)
    }

}
