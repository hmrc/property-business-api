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

package api.controllers.requestParsers.validators.validations

import api.models.errors.{DateFormatError, FromDateFormatError, MtdError, ToDateFormatError}

import java.time.LocalDate
import scala.util.{Failure, Success, Try}
import api.models.errors.ToDateOutOfRangeError
import api.models.errors.FromDateOutOfRangeError

object DateValidation {

  def validateOtherDate(field: Option[String], path: String): List[MtdError] =
    field match {
      case Some(field) =>
        Try {
          LocalDate.parse(field, dateFormat)
        } match {
          case Success(_) => Nil
          case Failure(_) => List(DateFormatError.copy(paths = Some(Seq(path))))
        }
      case _ => Nil
    }

  def validate(field: String, isFromDate: Boolean, minYear: Int, maxYear: Int): List[MtdError] = {
    Try {
      LocalDate.parse(field, dateFormat)
    } match {
      case Success(ld) => validateFromAndToDate(ld, isFromDate, minYear, maxYear)
      case Failure(_)  => returnFormatError(isFromDate)
    }
  }

  private def returnFormatError(isFromDate: Boolean) = {
    if (isFromDate) {
      List(FromDateFormatError)
    } else {
      List(ToDateFormatError)
    }
  }

  private def validateFromAndToDate(date: LocalDate, isFromDate: Boolean, minYear: Int, maxYear: Int): List[MtdError] = {
    if (isFromDate && date.getYear <= minYear) {
      List(FromDateOutOfRangeError)
    } else if (!isFromDate && date.getYear >= maxYear) {
      List(ToDateOutOfRangeError)
    } else {
      Nil
    }
  }

}
