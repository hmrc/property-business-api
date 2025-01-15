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

package v5.retrieveForeignPropertyAnnualSubmission

import cats.data.Validated.{Invalid, Valid}
import shared.controllers.validators.Validator
import v5.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionSchema.{Def1, Def2}
import v5.retrieveForeignPropertyAnnualSubmission.def1.Def1_RetrieveForeignPropertyAnnualSubmissionValidator
import v5.retrieveForeignPropertyAnnualSubmission.def2.Def2_RetrieveForeignPropertyAnnualSubmissionValidator
import v5.retrieveForeignPropertyAnnualSubmission.model.request.RetrieveForeignPropertyAnnualSubmissionRequestData

import javax.inject.Singleton

@Singleton
class RetrieveForeignPropertyAnnualSubmissionValidatorFactory {

  def validator(nino: String, businessId: String, taxYear: String): Validator[RetrieveForeignPropertyAnnualSubmissionRequestData] = {

    RetrieveForeignPropertyAnnualSubmissionSchema.schemaFor(Some(taxYear)) match {
      case Valid(Def1)     => new Def1_RetrieveForeignPropertyAnnualSubmissionValidator(nino, businessId, taxYear)
      case Valid(Def2)     => new Def2_RetrieveForeignPropertyAnnualSubmissionValidator(nino, businessId, taxYear)
      case Invalid(errors) => Validator.returningErrors(errors)
    }
  }

}
