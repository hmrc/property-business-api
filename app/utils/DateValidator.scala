/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import cats.data.Validated
import cats.data.Validated.{invalid, valid}
import cats.implicits.toFoldableOps
import common.controllers.validators.resolvers.ResolveFromAndToDates
import common.models.errors.RuleMissingSubmissionDatesError
import shared.controllers.validators.resolvers.ResolveIsoDate
import shared.models.errors.{FromDateFormatError, MtdError, ToDateFormatError}

object DateValidator {

  def validateFromAndToDates(fromDate: Option[String], toDate: Option[String]): Validated[Seq[MtdError], Unit] =
    (fromDate, toDate) match {
      case (Some(from), Some(to)) => ResolveFromAndToDates((from, to)).andThen(_ => valid(()))

      case (Some(from), None) =>
        Seq(
          ResolveIsoDate(from, FromDateFormatError),
          invalid(Seq(RuleMissingSubmissionDatesError))
        ).traverse_(identity)

      case (None, Some(to)) =>
        Seq(
          ResolveIsoDate(to, ToDateFormatError),
          invalid(Seq(RuleMissingSubmissionDatesError))
        ).traverse_(identity)

      case (None, None) => valid(())
    }

}
