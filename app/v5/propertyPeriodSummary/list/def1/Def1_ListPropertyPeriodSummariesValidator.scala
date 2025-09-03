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

package v5.propertyPeriodSummary.list.def1

import cats.data.Validated
import cats.implicits.*
import config.PropertyBusinessConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.*
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v5.propertyPeriodSummary.list.model.request.ListPropertyPeriodSummariesRequestData

import javax.inject.Inject

class Def1_ListPropertyPeriodSummariesValidator @Inject() (
    nino: String,
    businessId: String,
    taxYear: String
)(implicit config: PropertyBusinessConfig)
    extends Validator[ListPropertyPeriodSummariesRequestData] {

  private val resolveTaxYear = ResolveTaxYearMinMax((TaxYear.fromMtd(config.foreignMinimumTaxYear), TaxYear.fromMtd("2024-25")))

  def validate: Validated[Seq[MtdError], ListPropertyPeriodSummariesRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      resolveTaxYear(taxYear)
    ).mapN(ListPropertyPeriodSummariesRequestData)

}
