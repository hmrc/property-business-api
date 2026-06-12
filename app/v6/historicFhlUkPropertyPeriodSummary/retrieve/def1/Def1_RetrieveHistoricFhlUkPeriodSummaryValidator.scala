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

package v6.historicFhlUkPropertyPeriodSummary.retrieve.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.*
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.*
import common.controllers.validators.resolvers.ResolvePeriodId
import config.PropertyBusinessConfig
import v6.historicFhlUkPropertyPeriodSummary.retrieve.model.request.*

import javax.inject.Inject

class Def1_RetrieveHistoricFhlUkPeriodSummaryValidator @Inject() (nino: String, periodId: String)(implicit config: PropertyBusinessConfig)
    extends Validator[RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData] {

  private lazy val resolvePeriodId =
    new ResolvePeriodId(TaxYear.fromMtd(config.historicMinimumTaxYear), TaxYear.fromMtd(config.historicMaximumTaxYear))

  def validate: Validated[Seq[MtdError], RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      resolvePeriodId(periodId)
    ).mapN(Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData.apply)

}
