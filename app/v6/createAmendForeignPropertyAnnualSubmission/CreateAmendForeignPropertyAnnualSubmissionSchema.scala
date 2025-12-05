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

package v6.createAmendForeignPropertyAnnualSubmission

import cats.data.Validated
import cats.data.Validated.Valid
import config.PropertyBusinessConfig
import shared.controllers.validators.resolvers.ResolveTaxYearMinimum
import shared.models.domain.TaxYear
import shared.models.errors.MtdError

import scala.math.Ordered.orderingToOrdered

sealed trait CreateAmendForeignPropertyAnnualSubmissionSchema

object CreateAmendForeignPropertyAnnualSubmissionSchema {

  case object Def1 extends CreateAmendForeignPropertyAnnualSubmissionSchema

  case object Def2 extends CreateAmendForeignPropertyAnnualSubmissionSchema

  case object Def3 extends CreateAmendForeignPropertyAnnualSubmissionSchema

  def schemaFor(taxYearString: String)(implicit
      config: PropertyBusinessConfig): Validated[Seq[MtdError], CreateAmendForeignPropertyAnnualSubmissionSchema] =
    ResolveTaxYearMinimum(TaxYear.fromMtd(config.foreignMinimumTaxYear))(taxYearString) andThen schemaFor

  def schemaFor(taxYear: TaxYear): Validated[Seq[MtdError], CreateAmendForeignPropertyAnnualSubmissionSchema] = Valid {
    taxYear match {
      case ty if ty >= TaxYear.fromMtd("2026-27") => Def3
      case ty if ty == TaxYear.fromMtd("2025-26") => Def2
      case _                                      => Def1
    }
  }

}
