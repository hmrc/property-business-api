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

import api.models.domain.PeriodId
import api.models.errors.{MtdError, PeriodIdFormatError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

object ResolvePeriodId extends Resolver[String, PeriodId] with DateRangeFromStringResolving {

  override protected val startDateFormatError: MtdError = PeriodIdFormatError
  override protected val endDateFormatError: MtdError   = PeriodIdFormatError

  def apply(value: String, notUsedError: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], PeriodId] = {
    resolve(value, Some(PeriodIdFormatError), path)
      .map(dateRange => PeriodId(dateRange))
  }

  def apply(minimumTaxYear: Int,
            maximumTaxYear: Int,
            value: String,
            notUsedError: Option[MtdError],
            path: Option[String]): Validated[Seq[MtdError], PeriodId] = {
    resolve(value, Some(PeriodIdFormatError), path)
      .andThen { dateRange =>
        import dateRange.{startDateAsInt => fromYear, endDateAsInt => toYear}

        val fromYearIsValid = fromYear >= minimumTaxYear && fromYear <= maximumTaxYear
        val toYearIsValid   = toYear >= minimumTaxYear && toYear <= maximumTaxYear

        if (fromYearIsValid && toYearIsValid)
          Valid(dateRange)
        else
          Invalid(List(PeriodIdFormatError))
      }
      .map(dateRange => PeriodId(dateRange))
  }

}
