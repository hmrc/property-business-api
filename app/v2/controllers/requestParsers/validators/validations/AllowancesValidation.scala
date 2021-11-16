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

import v2.models.errors.{MtdError, RuleBothAllowancesSuppliedError}
import v2.models.request.amendUkPropertyAnnualSubmission.ukFhlProperty.UkFhlPropertyAllowances
import v2.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty.UkNonFhlPropertyAllowances

object AllowancesValidation {


  def validate(allowances: UkFhlPropertyAllowances, path: String): List[MtdError] = {
    allowances.propertyIncomeAllowance match {
      case None => NoValidationErrors
      case Some(_) => allowances match {
        case UkFhlPropertyAllowances(None, None, None, None, None, Some(_)) => NoValidationErrors
        case _ => List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq(path))))
      }
    }
  }
  def validate(allowances: UkNonFhlPropertyAllowances, path: String): List[MtdError] = {
    allowances.propertyIncomeAllowance match {
      case None => NoValidationErrors
      case Some(_) => allowances match {
        case UkNonFhlPropertyAllowances(None, None, None, None, None, None, None, Some(_), None, None) => NoValidationErrors
        case _ => List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq(path))))
      }
    }
  }

}
