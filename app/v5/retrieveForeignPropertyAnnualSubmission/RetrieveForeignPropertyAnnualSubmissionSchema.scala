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

package v5.retrieveForeignPropertyAnnualSubmission

import api.controllers.validators.resolvers.ResolveTaxYear
import api.models.domain.TaxYear
import api.models.errors.{InvalidTaxYearParameterError, MtdError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import play.api.libs.json.Reads
import schema.DownstreamReadable
import v5.retrieveForeignPropertyAnnualSubmission.def1.model.response.Def1_RetrieveForeignPropertyAnnualSubmissionResponse
import v5.retrieveForeignPropertyAnnualSubmission.def2.model.response.Def2_RetrieveForeignPropertyAnnualSubmissionResponse
import v5.retrieveForeignPropertyAnnualSubmission.model.response.RetrieveForeignPropertyAnnualSubmissionResponse

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

  private val preTysSchema = Def1

  def schemaFor(maybeTaxYear: Option[String]): Validated[Seq[MtdError], RetrieveForeignPropertyAnnualSubmissionSchema] =
    maybeTaxYear match {
      case Some(taxYearString) => ResolveTaxYear(taxYearString) andThen schemaFor
      case None                => Valid(preTysSchema)
    }

  def schemaFor(taxYear: TaxYear): Validated[Seq[MtdError], RetrieveForeignPropertyAnnualSubmissionSchema] = {
    if (taxYear < TaxYear.tysTaxYear) Invalid(Seq(InvalidTaxYearParameterError))
    else if (taxYear <= TaxYear.starting(2024)) Valid(Def1)
    else Valid(Def2)
  }

}
