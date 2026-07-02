/*
 * Copyright 2026 HM Revenue & Customs
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

package v6.createAmendUkPropertyAnnualSubmission

import api.controllers.validators.resolvers.ResolveTaxYearMinimum
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.Valid
import config.PropertyBusinessConfig

import scala.math.Ordered.orderingToOrdered

sealed trait CreateAmendUkPropertyAnnualSubmissionSchema

object CreateAmendUkPropertyAnnualSubmissionSchema {

  case object Def1 extends CreateAmendUkPropertyAnnualSubmissionSchema
  case object Def2 extends CreateAmendUkPropertyAnnualSubmissionSchema
  case object Def3 extends CreateAmendUkPropertyAnnualSubmissionSchema

  def schemaFor(taxYearString: String)(implicit
      config: PropertyBusinessConfig): Validated[Seq[MtdError], CreateAmendUkPropertyAnnualSubmissionSchema] =
    ResolveTaxYearMinimum(TaxYear.fromMtd(config.ukMinimumTaxYear))(taxYearString) andThen schemaFor

  def schemaFor(taxYear: TaxYear): Validated[Seq[MtdError], CreateAmendUkPropertyAnnualSubmissionSchema] = {
    if (taxYear < TaxYear.starting(2025)) Valid(Def1)
    else if (taxYear == TaxYear.starting(2025)) Valid(Def2)
    else Valid(Def3)
  }

}
