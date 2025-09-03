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

package v5.retrieveUkPropertyAnnualSubmission

import cats.data.Validated
import cats.data.Validated.Valid
import play.api.libs.json.Reads
import shared.controllers.validators.resolvers.ResolveTaxYear
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import shared.schema.DownstreamReadable
import v5.retrieveUkPropertyAnnualSubmission.def1.model.response.Def1_RetrieveUkPropertyAnnualSubmissionResponse
import v5.retrieveUkPropertyAnnualSubmission.def2.model.response.Def2_RetrieveUkPropertyAnnualSubmissionResponse
import v5.retrieveUkPropertyAnnualSubmission.model.response.*

import scala.math.Ordered.orderingToOrdered

sealed trait RetrieveUkPropertyAnnualSubmissionSchema extends DownstreamReadable[RetrieveUkPropertyAnnualSubmissionResponse]

object RetrieveUkPropertyAnnualSubmissionSchema {

  case object Def1 extends RetrieveUkPropertyAnnualSubmissionSchema {
    type DownstreamResp = Def1_RetrieveUkPropertyAnnualSubmissionResponse
    val connectorReads: Reads[DownstreamResp] = Def1_RetrieveUkPropertyAnnualSubmissionResponse.reads
  }

  case object Def2 extends RetrieveUkPropertyAnnualSubmissionSchema {
    type DownstreamResp = Def2_RetrieveUkPropertyAnnualSubmissionResponse
    val connectorReads: Reads[DownstreamResp] = Def2_RetrieveUkPropertyAnnualSubmissionResponse.reads
  }

  def schemaFor(maybeTaxYear: Option[String]): Validated[Seq[MtdError], RetrieveUkPropertyAnnualSubmissionSchema] =
    maybeTaxYear match {
      case Some(taxYearString) => ResolveTaxYear(taxYearString) andThen schemaFor
      case None                => Valid(Def1)
    }

  def schemaFor(taxYear: TaxYear): Validated[Seq[MtdError], RetrieveUkPropertyAnnualSubmissionSchema] = {
    if (taxYear >= TaxYear.fromMtd("2025-26")) Valid(Def2)
    else Valid(Def1)
  }

}
