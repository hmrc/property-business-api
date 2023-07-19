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

package v2.controllers.requestParsers.validators.validations

import api.controllers.requestParsers.validators.validations.NoValidationErrors
import api.models.errors.{MtdError, RuleBothExpensesSuppliedError}
import v2.models.request.amendHistoricNonFhlUkPiePeriodSummary.UkNonFhlPieExpenses
import v2.models.request.common.foreignFhlEea._
import v2.models.request.common.foreignPropertyEntry._
import v2.models.request.common.ukFhlPieProperty.UkFhlPieExpenses
import v2.models.request.common.ukFhlProperty.{UkFhlPropertyExpenses => CommonUkFhlPropertyExpenses}
import v2.models.request.common.ukNonFhlProperty.{UkNonFhlPropertyExpenses => CommonUkNonFhlPropertyExpenses}
import v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary.UkNonFhlPropertyExpenses

object ConsolidatedExpensesValidation {

  def validate(expenses: CreateForeignNonFhlPropertyExpenses, path: String): List[MtdError] = {
    expenses.consolidatedExpenses match {
      case None => NoValidationErrors
      case Some(_) =>
        expenses match {
          case CreateForeignNonFhlPropertyExpenses(None, None, None, None, None, None, _, _, None, Some(_)) =>
            NoValidationErrors
          case _ =>
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path))))
        }
    }
  }

  def validate(expenses: CreateForeignFhlEeaExpenses, path: String): List[MtdError] = {
    expenses.consolidatedExpenses match {
      case None => NoValidationErrors
      case Some(_) =>
        expenses match {
          case CreateForeignFhlEeaExpenses(None, None, None, None, None, None, None, Some(_)) =>
            NoValidationErrors
          case _ =>
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path))))
        }
    }
  }

  def validate(expenses: UkFhlPieExpenses, path: String): List[MtdError] = {
    expenses.consolidatedExpenses match {
      case None => NoValidationErrors
      case Some(_) =>
        expenses match {
          case UkFhlPieExpenses(None, None, None, None, None, None, Some(_), None, None) =>
            NoValidationErrors
          case _ =>
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path))))
        }
    }
  }

  def validate(expenses: CommonUkFhlPropertyExpenses, path: String): List[MtdError] = {
    expenses.consolidatedExpenses match {
      case None => NoValidationErrors
      case Some(_) =>
        expenses match {
          case CommonUkFhlPropertyExpenses(None, None, None, None, None, None, Some(_), None, None) =>
            NoValidationErrors
          case _ =>
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path))))
        }
    }
  }

  def validate(expenses: CommonUkNonFhlPropertyExpenses, path: String): List[MtdError] = {
    expenses.consolidatedExpenses match {
      case None => NoValidationErrors
      case Some(_) =>
        expenses match {
          case CommonUkNonFhlPropertyExpenses(None, None, None, None, None, None, None, None, None, None, Some(_)) =>
            NoValidationErrors
          case _ =>
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path))))
        }
    }
  }

  def validate(expenses: AmendForeignFhlEeaExpenses, path: String): List[MtdError] = {
    expenses.consolidatedExpenses match {
      case None => NoValidationErrors
      case Some(_) =>
        expenses match {
          case AmendForeignFhlEeaExpenses(None, None, None, None, None, None, None, Some(_)) =>
            NoValidationErrors
          case _ =>
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path))))
        }
    }
  }

  def validate(expenses: AmendForeignNonFhlPropertyExpenses, path: String): List[MtdError] = {
    expenses.consolidatedExpenses match {
      case None => NoValidationErrors
      case Some(_) =>
        expenses match {
          case AmendForeignNonFhlPropertyExpenses(None, None, None, None, None, None, _, _, None, Some(_)) =>
            NoValidationErrors
          case _ =>
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path))))
        }
    }
  }

  def validate(expenses: UkNonFhlPropertyExpenses, path: String): List[MtdError] = {
    expenses.consolidatedExpenses match {
      case None => NoValidationErrors
      case Some(_) =>
        expenses match {
          case UkNonFhlPropertyExpenses(None, None, None, None, None, None, None, None, None, None, Some(_)) =>
            NoValidationErrors
          case _ =>
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path))))
        }
    }
  }

  def validate(expenses: UkNonFhlPieExpenses, path: String): List[MtdError] = {
    expenses.consolidatedExpenses match {
      case None => NoValidationErrors
      case Some(_) =>
        expenses match {
          case UkNonFhlPieExpenses(None, None, None, None, None, None, Some(_), None, None, None, None) =>
            NoValidationErrors
          case _ =>
            List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path))))
        }
    }
  }

}
