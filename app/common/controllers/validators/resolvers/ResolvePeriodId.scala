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

package common.controllers.validators.resolvers

import cats.data.Validated
import cats.data.Validated.Invalid
import common.models.domain.PeriodId
import common.models.errors.PeriodIdFormatError
import shared.controllers.validators.resolvers.{ResolveDateRange, ResolverSupport}
import shared.models.domain.{DateRange, TaxYear}
import shared.models.errors.MtdError

import java.time.LocalDate
import scala.math.Ordering.Implicits.infixOrderingOps

class ResolvePeriodId(minimumTaxYear: TaxYear, maximumTaxYear: TaxYear) extends ResolverSupport {

  private val resolveDateRange = ResolveDateRange(
    startDateFormatError = PeriodIdFormatError,
    endDateFormatError = PeriodIdFormatError,
    endBeforeStartDateError = PeriodIdFormatError
  )

  private val minDate = minimumTaxYear.startDate
  private val maxDate = maximumTaxYear.endDate

  private val splitAndResolveDateRange: Resolver[String, DateRange] = { value =>
    value.split('_') match {
      case Array(from, to) => resolveDateRange.resolver(from -> to)
      case _               => Invalid(List(PeriodIdFormatError))
    }
  }

  private val withinLimits: Validator[DateRange] = {
    def inRange(date: LocalDate) = minDate <= date && date <= maxDate

    satisfies(PeriodIdFormatError)(dateRange => inRange(dateRange.startDate) && inRange(dateRange.endDate))
  }

  val resolver: Resolver[String, PeriodId] =
    (splitAndResolveDateRange thenValidate withinLimits).map(dateRange => PeriodId(dateRange))

  def apply(value: String): Validated[Seq[MtdError], PeriodId] = resolver(value)
}
