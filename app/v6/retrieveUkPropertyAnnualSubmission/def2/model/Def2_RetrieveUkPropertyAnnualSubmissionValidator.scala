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

package v6.retrieveUkPropertyAnnualSubmission.def2.model

import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveTaxYear}
import shared.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import v6.retrieveUkPropertyAnnualSubmission.def2.model.request.Def2_RetrieveUkPropertyAnnualSubmissionRequestData
import v6.retrieveUkPropertyAnnualSubmission.model.request._

import javax.inject.Inject

class Def2_RetrieveUkPropertyAnnualSubmissionValidator @Inject() (nino: String, businessId: String, taxYear: String)
    extends Validator[RetrieveUkPropertyAnnualSubmissionRequestData] {

  def validate: Validated[Seq[MtdError], RetrieveUkPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveTaxYear(taxYear)
    ).mapN(Def2_RetrieveUkPropertyAnnualSubmissionRequestData)

}
