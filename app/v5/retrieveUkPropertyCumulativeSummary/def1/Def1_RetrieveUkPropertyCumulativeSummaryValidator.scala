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

package v5.retrieveUkPropertyCumulativeSummary.def1

import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveTaxYear}
import shared.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import v5.retrieveUkPropertyCumulativeSummary.def1.model.request.Def1_RetrieveUkPropertyCumulativeSummaryRequestData
import v5.retrieveUkPropertyCumulativeSummary.model.request.RetrieveUkPropertyCumulativeSummaryRequestData

import javax.inject.Inject

class Def1_RetrieveUkPropertyCumulativeSummaryValidator @Inject() (
    nino: String,
    businessId: String,
    taxYear: String
) extends Validator[RetrieveUkPropertyCumulativeSummaryRequestData] {

  def validate: Validated[Seq[MtdError], RetrieveUkPropertyCumulativeSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveTaxYear(taxYear)
    ).mapN(Def1_RetrieveUkPropertyCumulativeSummaryRequestData)

}
