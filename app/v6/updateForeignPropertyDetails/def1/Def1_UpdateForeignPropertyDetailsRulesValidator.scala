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

package v6.updateForeignPropertyDetails.def1

import cats.data.Validated
import cats.data.Validated.Invalid
import common.models.errors.*
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.{ResolveIsoDate, ResolveStringPattern, ResolverSupport}
import shared.models.domain.TaxYear
import shared.models.errors.*
import v6.updateForeignPropertyDetails.def1.model.request.{Def1_UpdateForeignPropertyDetailsRequestData, EndReason}

import scala.util.matching.Regex

object Def1_UpdateForeignPropertyDetailsRulesValidator extends RulesValidator[Def1_UpdateForeignPropertyDetailsRequestData] with ResolverSupport {

  private val propertyNameRegex: Regex = "^.{1,105}$".r

  def validateBusinessRules(
      parsed: Def1_UpdateForeignPropertyDetailsRequestData): Validated[Seq[MtdError], Def1_UpdateForeignPropertyDetailsRequestData] = {
    import parsed.body.*

    combine(
      ResolveStringPattern(propertyName, propertyNameRegex, PropertyNameFormatError),
      resolveEndReason(endReason),
      validateEndDate(endDate, parsed.taxYear),
      validateEndDateAndReason(endDate, endReason)
    ).onSuccess(parsed)
  }

  private def validateEndDate(endDate: Option[String], taxYear: TaxYear): Validated[Seq[MtdError], Unit] = {
    endDate.fold(valid) { date =>
      ResolveIsoDate(date, EndDateFormatError).andThen { parsedDate =>
        if (parsedDate.isBefore(taxYear.startDate)) {
          Invalid(List(RuleEndDateBeforeTaxYearStartError))
        } else if (parsedDate.isAfter(taxYear.endDate)) {
          Invalid(List(RuleEndDateAfterTaxYearEndError))
        } else {
          valid
        }
      }
    }
  }

  private def resolveEndReason(endReason: Option[String]): Validated[Seq[MtdError], Option[EndReason]] = {
    val resolver: Resolver[String, EndReason] = resolvePartialFunction(EndReasonFormatError)(EndReason.parser)
    resolver.resolveOptionally(endReason)
  }

  private def validateEndDateAndReason(endDate: Option[String], endReason: Option[String]): Validated[Seq[MtdError], Unit] = {
    (endDate, endReason) match {
      case (Some(_), None) | (None, Some(_)) => Invalid(List(RuleMissingEndDetailsError))
      case _                                 => valid
    }
  }

}
