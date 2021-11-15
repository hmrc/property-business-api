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

import v2.models.errors.{MtdError, RuleBothExpensesSuppliedError}
import v2.models.request.common.foreignFhlEea._
import v2.models.request.common.foreignPropertyEntry._
import v2.models.request.common.ukFhlProperty.UkFhlPropertyExpenses
import v2.models.request.common.ukNonFhlProperty.UkNonFhlPropertyExpenses

object ConsolidatedExpensesValidation {

  def validate(expenditure: ForeignPropertyExpenditure, path: String): List[MtdError] = {
    expenditure.consolidatedExpenses match {
      case None => NoValidationErrors
      case Some(_) =>
        expenditure match {
          case ForeignPropertyExpenditure(None, None, None, None, None, None, _, _, None, Some(_)) => NoValidationErrors
          case _                                                                                   =>
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path))))
        }
    }
  }

  def validate(expenditure: ForeignFhlEeaExpenditure, path: String): List[MtdError] = {
    expenditure.consolidatedExpenses match {
      case None => NoValidationErrors
      case Some(_) =>
        expenditure match {
          case ForeignFhlEeaExpenditure(None, None, None, None, None, None, None, Some(_)) => NoValidationErrors
          case _                                                                           => List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path))))
        }
    }
  }

  def validate(expenses: UkFhlPropertyExpenses, path: String): List[MtdError] = {
    expenses.consolidatedExpense match {
      case None => NoValidationErrors
      case Some(_) =>
        expenses match {
          case UkFhlPropertyExpenses(None, None, None, None, None, None, Some(_), None, None) => NoValidationErrors
          case _                                                                              => List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path))))
        }
    }
  }

  def validate(expenses: UkNonFhlPropertyExpenses, path: String): List[MtdError] = {
    expenses.consolidatedExpense match {
      case None => NoValidationErrors
      case Some(_) =>
        expenses match {
          case UkNonFhlPropertyExpenses(None, None, None, None, None, None, None, None, None, None, Some(_)) => NoValidationErrors
          case _                                                                                             => List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path))))
        }
    }
  }
}
