/*
 * Copyright 2021 HM Revenue & Customs
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

package v2.controllers.requestParsers.validators.validations

import java.time.LocalDate

import v2.models.errors.{DateFormatError, FromDateFormatError, MtdError, ToDateFormatError}

import scala.util.{Failure, Success, Try}

object DateValidation {


  def validateOtherDate(field: Option[String], path: String): List[MtdError] =

  field match {
    case Some(field) => Try {
      LocalDate.parse(field, dateFormat)
    } match {
      case Success(_) => NoValidationErrors
      case Failure(_) => List(DateFormatError.copy(paths = Some(Seq(path))))
    }
    case _ => NoValidationErrors
  }

  def validate(field: String, isFromDate: Boolean): List[MtdError] = Try {
    LocalDate.parse(field, dateFormat)
  } match {
    case Success(_) => NoValidationErrors
    case Failure(_) => if (isFromDate) {
      List(FromDateFormatError)
    } else {
      List(ToDateFormatError)
    }
  }

}
