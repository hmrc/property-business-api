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

package v6.retrieveForeignPropertyAnnualSubmission

import cats.data.Validated
import cats.data.Validated.Valid
import config.PropertyBusinessConfig
import play.api.libs.json.Reads
import shared.controllers.validators.resolvers.ResolveTaxYearMinimum
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import shared.schema.DownstreamReadable
import v6.retrieveForeignPropertyAnnualSubmission.def1.model.response.Def1_RetrieveForeignPropertyAnnualSubmissionResponse
import v6.retrieveForeignPropertyAnnualSubmission.def2.model.response.Def2_RetrieveForeignPropertyAnnualSubmissionResponse
import v6.retrieveForeignPropertyAnnualSubmission.def3.model.response.Def3_RetrieveForeignPropertyAnnualSubmissionResponse
import v6.retrieveForeignPropertyAnnualSubmission.model.response.RetrieveForeignPropertyAnnualSubmissionResponse

import scala.math.Ordered.orderingToOrdered

sealed trait RetrieveForeignPropertyAnnualSubmissionSchema extends DownstreamReadable[RetrieveForeignPropertyAnnualSubmissionResponse]

object RetrieveForeignPropertyAnnualSubmissionSchema {

  case object Def1 extends RetrieveForeignPropertyAnnualSubmissionSchema {
    type DownstreamResp = Def1_RetrieveForeignPropertyAnnualSubmissionResponse
    val connectorReads: Reads[DownstreamResp] = Def1_RetrieveForeignPropertyAnnualSubmissionResponse.reads
  }

  case object Def2 extends RetrieveForeignPropertyAnnualSubmissionSchema {
    type DownstreamResp = Def2_RetrieveForeignPropertyAnnualSubmissionResponse
    val connectorReads: Reads[DownstreamResp] = Def2_RetrieveForeignPropertyAnnualSubmissionResponse.reads
  }

  case object Def3 extends RetrieveForeignPropertyAnnualSubmissionSchema {
    type DownstreamResp = Def3_RetrieveForeignPropertyAnnualSubmissionResponse
    val connectorReads: Reads[DownstreamResp] = Def3_RetrieveForeignPropertyAnnualSubmissionResponse.format.reads(_)
  }

  def schemaFor(taxYearString: String)(implicit
      config: PropertyBusinessConfig): Validated[Seq[MtdError], RetrieveForeignPropertyAnnualSubmissionSchema] =
    ResolveTaxYearMinimum(TaxYear.fromMtd(config.foreignMinimumTaxYear))(taxYearString) andThen schemaFor

  def schemaFor(taxYear: TaxYear): Validated[Seq[MtdError], RetrieveForeignPropertyAnnualSubmissionSchema] = Valid {
    taxYear match {
      case ty if ty >= TaxYear.fromMtd("2026-27") => Def3
      case ty if ty == TaxYear.fromMtd("2025-26") => Def2
      case _                                      => Def1
    }
  }

}
