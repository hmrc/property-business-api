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

package v2.controllers.validators.resolvers

import api.controllers.validators.resolvers.{DateRangeResolving, Resolver}
import api.models.domain.DateRange
import api.models.errors.{FromDateFormatError, MtdError, RuleToDateBeforeFromDateError, ToDateFormatError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.toFoldableOps

class ResolveFromAndToDates(minimumTaxYear: Int, maximumTaxYear: Int) extends Resolver[(String, String), DateRange] {

  private val resolveDateRange = new DateRangeResolving {
    override protected val startDateFormatError: MtdError    = FromDateFormatError
    override protected val endDateFormatError: MtdError      = ToDateFormatError
    override protected val endBeforeStartDateError: MtdError = RuleToDateBeforeFromDateError
  }

  override def apply(value: (String, String), error: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], DateRange] = {
    resolveDateRange(value, error, path) andThen { dateRange =>
      import dateRange.{endDateAsInt => toYear, startDateAsInt => fromYear}

      val validatedFromDate = if (fromYear < minimumTaxYear) Invalid(List(FromDateFormatError)) else Valid(())
      val validatedToDate   = if (toYear >= maximumTaxYear) Invalid(List(ToDateFormatError)) else Valid(())

      List(
        validatedFromDate,
        validatedToDate
      ).traverse_(identity).map(_ => dateRange)

    }
  }

}
