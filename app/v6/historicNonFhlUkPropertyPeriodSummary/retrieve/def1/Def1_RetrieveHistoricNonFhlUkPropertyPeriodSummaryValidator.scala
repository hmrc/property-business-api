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

package v6.historicNonFhlUkPropertyPeriodSummary.retrieve.def1

import cats.data.Validated
import cats.implicits.catsSyntaxTuple2Semigroupal
import common.controllers.validators.resolvers.ResolvePeriodId
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers._
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v6.historicNonFhlUkPropertyPeriodSummary.retrieve.model.request.{
  Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData,
  RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData
}

class Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidator(
    nino: String,
    periodId: String
) extends Validator[RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData] {

  private val resolvePeriodId = new ResolvePeriodId(TaxYear.fromMtd("2017-18"), TaxYear.fromMtd("2021-22"))

  def validate: Validated[Seq[MtdError], RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      resolvePeriodId(periodId)
    ).mapN(Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData)

}
