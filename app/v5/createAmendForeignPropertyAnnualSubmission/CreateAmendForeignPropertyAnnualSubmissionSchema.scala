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

package v5.createAmendForeignPropertyAnnualSubmission

import shared.controllers.validators.resolvers.ResolveTaxYear
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.Valid

import scala.math.Ordered.orderingToOrdered

sealed trait CreateAmendForeignPropertyAnnualSubmissionSchema

object CreateAmendForeignPropertyAnnualSubmissionSchema {

  case object Def1 extends CreateAmendForeignPropertyAnnualSubmissionSchema

  case object Def2 extends CreateAmendForeignPropertyAnnualSubmissionSchema

  private val preTysSchema = Def1

  def schemaFor(maybeTaxYear: Option[String]): Validated[Seq[MtdError], CreateAmendForeignPropertyAnnualSubmissionSchema] =
    maybeTaxYear match {
      case Some(taxYearString) => ResolveTaxYear(taxYearString) andThen schemaFor
      case None                => Valid(preTysSchema)
    }

  def schemaFor(taxYear: TaxYear): Validated[Seq[MtdError], CreateAmendForeignPropertyAnnualSubmissionSchema] = {
    if (taxYear < TaxYear.starting(2025)) Valid(Def1)
    else Valid(Def2)
  }

}
