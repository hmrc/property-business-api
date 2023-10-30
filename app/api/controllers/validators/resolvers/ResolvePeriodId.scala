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

import api.models.domain.{DateRange, PeriodId, TaxYear}
import api.models.errors.{MtdError, PeriodIdFormatError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

import java.time.LocalDate
import scala.math.Ordering.Implicits.infixOrderingOps

class ResolvePeriodId(minimumTaxYear: TaxYear, maximumTaxYear: TaxYear) {

  private val resolveDateRange = new DateRangeResolving {
    override protected val startDateFormatError: MtdError    = PeriodIdFormatError
    override protected val endDateFormatError: MtdError      = PeriodIdFormatError
    override protected val endBeforeStartDateError: MtdError = PeriodIdFormatError
  }

  private val minDate = minimumTaxYear.startDate
  private val maxDate = maximumTaxYear.endDate

  def apply(value: String): Validated[Seq[MtdError], PeriodId] = {
    splitAndResolveDateRange(value)
      .andThen { dateRange =>
        if (inRange(dateRange.startDate) && inRange(dateRange.endDate))
          Valid(dateRange)
        else
          Invalid(List(PeriodIdFormatError))
      }
      .map(dateRange => PeriodId(dateRange))
  }

  private def inRange(date: LocalDate) = minDate <= date && date <= maxDate

  private def splitAndResolveDateRange(value: String): Validated[Seq[MtdError], DateRange] = {
    value.split('_') match {
      case Array(from, to) => resolveDateRange(from -> to, Some(PeriodIdFormatError), None)
      case _               => Invalid(List(PeriodIdFormatError))
    }
  }

}
