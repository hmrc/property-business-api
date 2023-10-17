/*
 * Copyright 2023 HM Revenue & Customs
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

package api.controllers.validators.resolvers

import api.models.domain.{DateRange, PeriodId}
import api.models.errors.{MtdError, PeriodIdFormatError, RuleTaxYearRangeInvalid}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

class ResolvePeriodId(minimumTaxYear: Int, maximumTaxYear: Int) extends Resolver[String, PeriodId] with DateRangeResolving {

  override protected val startDateFormatError: MtdError    = PeriodIdFormatError
  override protected val endDateFormatError: MtdError      = PeriodIdFormatError
  override protected val endBeforeStartDateError: MtdError = PeriodIdFormatError

  def apply(value: String, notUsedError: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], PeriodId] = {
    splitAndResolve(value, Some(PeriodIdFormatError), path)
      .andThen { dateRange =>
        import dateRange.{endDateAsInt => toYear, startDateAsInt => fromYear}

        val fromYearIsValid = fromYear >= minimumTaxYear && fromYear <= maximumTaxYear
        val toYearIsValid   = toYear >= minimumTaxYear && toYear <= maximumTaxYear

        if (fromYearIsValid && toYearIsValid)
          Valid(dateRange)
        else
          Invalid(List(PeriodIdFormatError))
      }
      .map(dateRange => PeriodId(dateRange))
  }

  private def splitAndResolve(value: String, maybeError: Option[MtdError], maybePath: Option[String]): Validated[Seq[MtdError], DateRange] = {
    value.split('_') match {
      case Array(from, to) => resolve(from -> to, maybeError, maybePath)
      case _ => Invalid(List(maybeError.getOrElse(RuleTaxYearRangeInvalid)))
    }
  }

}
