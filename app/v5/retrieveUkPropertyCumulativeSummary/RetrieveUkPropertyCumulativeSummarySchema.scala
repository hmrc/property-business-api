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

package v5.retrieveUkPropertyCumulativeSummary

import api.controllers.validators.resolvers.ResolveTaxYear
import api.models.domain.TaxYear
import api.models.errors.{MtdError, RuleTaxYearNotSupportedError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import play.api.libs.json.Reads
import schema.DownstreamReadable
import v5.retrieveUkPropertyCumulativeSummary.def1.model.response.Def1_RetrieveUkPropertyCumulativeSummaryResponse
import v5.retrieveUkPropertyCumulativeSummary.model.response.RetrieveUkPropertyCumulativeSummaryResponse

import scala.math.Ordered.orderingToOrdered

sealed trait RetrieveUkPropertyCumulativeSummarySchema extends DownstreamReadable[RetrieveUkPropertyCumulativeSummaryResponse]

object RetrieveUkPropertyCumulativeSummarySchema {

  case object Def1 extends RetrieveUkPropertyCumulativeSummarySchema {
    type DownstreamResp = Def1_RetrieveUkPropertyCumulativeSummaryResponse
    val connectorReads: Reads[DownstreamResp] = Def1_RetrieveUkPropertyCumulativeSummaryResponse.reads
  }

  def schemaFor(taxYearString: String): Validated[Seq[MtdError], RetrieveUkPropertyCumulativeSummarySchema] =
    ResolveTaxYear(taxYearString) andThen schemaFor

  def schemaFor(taxYear: TaxYear): Validated[Seq[MtdError], RetrieveUkPropertyCumulativeSummarySchema] = {
    if (taxYear < TaxYear.starting(2025)) Invalid(Seq(RuleTaxYearNotSupportedError))
    else Valid(Def1)
  }

}