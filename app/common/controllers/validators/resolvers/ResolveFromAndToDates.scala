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

import shared.models.domain.DateRange
import shared.models.errors._
import cats.data.Validated
import common.models.errors.RuleToDateBeforeFromDateError
import shared.controllers.validators.resolvers.{ResolveDateRange, ResolverSupport}

object ResolveFromAndToDates extends ResolverSupport {

  private val minimumYear = 1900
  private val maximumYear = 2099

  private val resolveDateRange = ResolveDateRange(
    startDateFormatError = FromDateFormatError,
    endDateFormatError = ToDateFormatError,
    endBeforeStartDateError = RuleToDateBeforeFromDateError
  )

  val resolver: Resolver[(String, String), DateRange] = resolveDateRange.withYearsLimitedTo(minimumYear, maximumYear)

  def apply(value: (String, String)): Validated[Seq[MtdError], DateRange] = resolver(value)

}
