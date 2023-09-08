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

trait ResolvingPeriodId extends Resolver[String, PeriodId] {
  private val periodIdLength = "YYYY-MM-DD_YYYY-MM-DD".length
  private val taxDateRegex   = "20\\d{2}-\\d{2}-\\d{2}".r

  protected def resolve(value: String): Validated[Seq[MtdError], PeriodId] = {
    if (value.length.equals(periodIdLength)) {
      val fromDate   = value.substring(0, 10)
      val toDate     = value.substring(11, 21)
      val underscore = value.substring(10, 11)

      if (taxDateRegex.matches(fromDate) && taxDateRegex.matches(toDate) && underscore.matches("_")) {
        ResolveDateRange((fromDate, toDate), None, None) match {
          case Valid(_)   => Valid(PeriodId(value))
          case Invalid(_) => Invalid(List(PeriodIdFormatError))
        }
      } else {
        Invalid(List(PeriodIdFormatError))
      }
    } else {
      Invalid(List(PeriodIdFormatError))
    }
  }

}

object ResolvePeriodId extends ResolvingPeriodId {

  def apply(value: String, error: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], PeriodId] =
    resolve(value)

  def apply(minimumTaxYear: Int, maximumTaxYear: Int, value: String): Validated[Seq[MtdError], PeriodId] = {
    resolve(value)
      .andThen { periodId =>
        {
          val fromYear = periodId.from.substring(0, 4).toInt
          val toYear   = periodId.to.substring(0, 4).toInt

          val fromYearIsValid = fromYear >= minimumTaxYear && fromYear <= maximumTaxYear
          val toYearIsValid   = toYear >= minimumTaxYear && toYear <= maximumTaxYear

          if (fromYearIsValid && toYearIsValid)
            Valid(periodId)
          else
            Invalid(List(PeriodIdFormatError))
        }
      }
  }

}
