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

import api.models.domain.DateRange
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._

import java.time.LocalDate

trait DateRangeResolving {

  protected val startDateFormatError: MtdError    = StartDateFormatError
  protected val endDateFormatError: MtdError      = EndDateFormatError
  protected val endBeforeStartDateError: MtdError = RuleEndBeforeStartDateError

  protected def resolve(value: (String, String), maybeError: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], DateRange] = {

    def resolveDateRange(parsedStartDate: LocalDate, parsedEndDate: LocalDate): Validated[Seq[MtdError], DateRange] = {
      val startDateEpochTime = parsedStartDate.toEpochDay
      val endDateEpochTime   = parsedEndDate.toEpochDay

      if ((endDateEpochTime - startDateEpochTime) <= 0)
        Invalid(List(maybeError.getOrElse(endBeforeStartDateError)))
      else
        Valid(DateRange(parsedStartDate, parsedEndDate))

    }

    val (startDate, endDate) = value
    (
      ResolveIsoDate(startDate, startDateFormatError),
      ResolveIsoDate(endDate, endDateFormatError)
    ).mapN(resolveDateRange).andThen(identity)
  }

}

object ResolveDateRange extends Resolver[(String, String), DateRange] with DateRangeResolving {

  def apply(value: (String, String), maybeError: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], DateRange] = {
    resolve(value, maybeError, path)
  }

}

trait DateRangeFromStringResolving extends DateRangeResolving {

  protected def resolve(value: String, maybeError: Option[MtdError], maybePath: Option[String]): Validated[Seq[MtdError], DateRange] = {
    value.split('_') match {
      case Array(from, to) => resolve(from -> to, maybeError, maybePath)
      case _               => Invalid(List(maybeError.getOrElse(RuleTaxYearRangeInvalid)))
    }
  }

}

/** Resolves a date range from a single input string in the format "2023-01-01_2023-01-01"
  */
object ResolveDateRangeFromString extends Resolver[String, DateRange] with DateRangeFromStringResolving {

  def apply(value: String, maybeError: Option[MtdError], maybePath: Option[String]): Validated[Seq[MtdError], DateRange] = {
    resolve(value, maybeError, maybePath)
  }

}
