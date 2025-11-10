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

package v6.retrieveForeignPropertyDetails

import cats.data.Validated
import play.api.libs.json.Reads
import shared.controllers.validators.resolvers.ResolveTaxYearMinimum
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import shared.schema.DownstreamReadable
import v6.retrieveForeignPropertyDetails.def1.model.response.Def1_RetrieveForeignPropertyDetailsResponse
import v6.retrieveForeignPropertyDetails.model.response.RetrieveForeignPropertyDetailsResponse

sealed trait RetrieveForeignPropertyDetailsSchema extends DownstreamReadable[RetrieveForeignPropertyDetailsResponse]

object RetrieveForeignPropertyDetailsSchema {

  case object Def1 extends RetrieveForeignPropertyDetailsSchema {
    type DownstreamResp = Def1_RetrieveForeignPropertyDetailsResponse
    val connectorReads: Reads[DownstreamResp] = Def1_RetrieveForeignPropertyDetailsResponse.format.reads(_)
  }

  def schemaFor(taxYear: String): Validated[Seq[MtdError], RetrieveForeignPropertyDetailsSchema] =
    ResolveTaxYearMinimum(TaxYear.fromMtd("2026-27"))(taxYear).map(_ => Def1)

}
