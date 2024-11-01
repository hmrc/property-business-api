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

package v5.retrieveUkPropertyAnnualSubmission

import api.controllers.validators.resolvers.ResolveTaxYear
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.Valid
import play.api.libs.json.Reads
import schema.DownstreamReadable
import v5.retrieveUkPropertyAnnualSubmission.model.response._

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
