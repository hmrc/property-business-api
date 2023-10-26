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
import api.models.errors.{FromDateFormatError, MtdError, RuleToDateBeforeFromDateError, ToDateFormatError}
import cats.data.Validated

object ResolveFromAndToDates extends Resolvers {

  private val minimumYear = 1900
  private val maximumYear = 2099

  private val resolveDateRange = ResolveDateRange(
    startDateFormatError = FromDateFormatError,
    endDateFormatError = ToDateFormatError,
    endBeforeStartDateError = RuleToDateBeforeFromDateError
  )

  private val withinLimits = combinedValidator[DateRange](
    satisfies(FromDateFormatError)(_.startDate.getYear >= minimumYear),
    satisfies(ToDateFormatError)(_.endDate.getYear <= maximumYear)
  )

  val resolver: SimpleResolver[(String, String), DateRange] = resolveDateRange.resolver thenValidate withinLimits

  def apply(value: (String, String)): Validated[Seq[MtdError], DateRange] = resolver(value)
}
