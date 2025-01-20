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

package v6.retrieveUkPropertyAnnualSubmission

import cats.data.Validated._
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v6.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionSchema.{Def1, Def2}
import v6.retrieveUkPropertyAnnualSubmission.def1.model.Def1_RetrieveUkPropertyAnnualSubmissionValidator
import v6.retrieveUkPropertyAnnualSubmission.def2.model.Def2_RetrieveUkPropertyAnnualSubmissionValidator
import v6.retrieveUkPropertyAnnualSubmission.model.request.RetrieveUkPropertyAnnualSubmissionRequestData

class RetrieveUkPropertyAnnualSubmissionValidatorFactory {

  def validator(nino: String, businessId: String, taxYear: String): Validator[RetrieveUkPropertyAnnualSubmissionRequestData] =
    RetrieveUkPropertyAnnualSubmissionSchema.schemaFor(Some(taxYear)) match {
      case Valid(Def1)                    => new Def1_RetrieveUkPropertyAnnualSubmissionValidator(nino, businessId, taxYear)
      case Valid(Def2)                    => new Def2_RetrieveUkPropertyAnnualSubmissionValidator(nino, businessId, taxYear)
      case Invalid(errors: Seq[MtdError]) => Validator.returningErrors(errors)
    }

}
