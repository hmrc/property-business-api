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

package v6.retrieveForeignPropertyAnnualSubmission.def1

import cats.data.Validated
import cats.implicits.*
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v6.retrieveForeignPropertyAnnualSubmission.def1.request.Def1_RetrieveForeignPropertyAnnualSubmissionRequestData
import v6.retrieveForeignPropertyAnnualSubmission.model.request.RetrieveForeignPropertyAnnualSubmissionRequestData

import javax.inject.Inject

class Def1_RetrieveForeignPropertyAnnualSubmissionValidator @Inject() (nino: String, businessId: String, taxYear: String)
    extends Validator[RetrieveForeignPropertyAnnualSubmissionRequestData] {

  def validate: Validated[Seq[MtdError], RetrieveForeignPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId)
    ).mapN { (validNino, validBusinessId) =>
      Def1_RetrieveForeignPropertyAnnualSubmissionRequestData(
        nino = validNino,
        businessId = validBusinessId,
        taxYear = TaxYear.fromMtd(taxYear)
      )
    }

}
