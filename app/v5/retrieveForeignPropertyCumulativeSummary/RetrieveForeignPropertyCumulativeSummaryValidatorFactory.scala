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

package v5.retrieveForeignPropertyCumulativeSummary

import cats.data.Validated.{Invalid, Valid}
import shared.controllers.validators.Validator
import v5.retrieveForeignPropertyCumulativeSummary.def1.Def1_RetrieveForeignPropertyCumulativeSummaryValidator
import v5.retrieveForeignPropertyCumulativeSummary.model.request.RetrieveForeignPropertyCumulativeSummaryRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveForeignPropertyCumulativeSummaryValidatorFactory @Inject() {

  def validator(nino: String, businessId: String, taxYear: String): Validator[RetrieveForeignPropertyCumulativeSummaryRequestData] =
    RetrieveForeignPropertyCumulativeSummarySchema.schemaFor(taxYear) match {
      case Valid(RetrieveForeignPropertyCumulativeSummarySchema.Def1) =>
        new Def1_RetrieveForeignPropertyCumulativeSummaryValidator(nino, businessId, taxYear)
      case Invalid(errors) => Validator.returningErrors(errors)
    }

}
