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

package v6.createAmendForeignPropertyCumulativePeriodSummary

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import shared.controllers.validators.resolvers.ResolveTaxYear
import shared.models.domain.TaxYear
import shared.models.errors.{MtdError, RuleTaxYearNotSupportedError}

import scala.math.Ordered.orderingToOrdered

sealed trait CreateAmendForeignPropertyCumulativePeriodSummarySchema

object CreateAmendForeignPropertyCumulativePeriodSummarySchema {
  case object Def1 extends CreateAmendForeignPropertyCumulativePeriodSummarySchema

  def schemaFor(taxYear: String): Validated[Seq[MtdError], CreateAmendForeignPropertyCumulativePeriodSummarySchema] =
    ResolveTaxYear(taxYear) andThen schemaFor

  def schemaFor(taxYear: TaxYear): Validated[Seq[MtdError], CreateAmendForeignPropertyCumulativePeriodSummarySchema] = {
    if (taxYear < TaxYear.starting(2025)) Invalid(Seq(RuleTaxYearNotSupportedError))
    else Valid(Def1)
  }

}
