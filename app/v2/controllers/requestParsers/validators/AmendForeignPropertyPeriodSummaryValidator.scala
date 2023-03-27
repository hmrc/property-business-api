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

package v2.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations.{BusinessIdValidation, NinoValidation, SubmissionIdValidation}
import api.models.errors.{MtdError, RuleDuplicateCountryCodeError}
import config.AppConfig
import v2.controllers.requestParsers.validators.validations._
import v2.models.request.amendForeignPropertyPeriodSummary.{AmendForeignPropertyPeriodSummaryRawData, AmendForeignPropertyPeriodSummaryRequestBody}
import v2.models.request.common.foreignFhlEea.{AmendForeignFhlEea, AmendForeignFhlEeaExpenses}
import v2.models.request.common.foreignPropertyEntry.{AmendForeignNonFhlPropertyEntry, AmendForeignNonFhlPropertyExpenses}

import javax.inject.{Inject, Singleton}

@Singleton
class AmendForeignPropertyPeriodSummaryValidator @Inject()(appConfig: AppConfig) extends Validator[AmendForeignPropertyPeriodSummaryRawData] {

  private lazy val minTaxYear = appConfig.minimumTaxV2Foreign
  private val validationSet   = List(parameterFormatValidation, bodyFormatValidation, bodyFieldValidation)

  private def parameterFormatValidation: AmendForeignPropertyPeriodSummaryRawData => List[List[MtdError]] =
    (data: AmendForeignPropertyPeriodSummaryRawData) => {
      List(
        NinoValidation.validate(data.nino),
        TaxYearValidation.validate(minTaxYear, data.taxYear),
        BusinessIdValidation.validate(data.businessId),
        SubmissionIdValidation.validate(data.submissionId)
      )
    }

  private def bodyFormatValidation: AmendForeignPropertyPeriodSummaryRawData => List[List[MtdError]] = { data =>
    JsonFormatValidation.validateAndCheckNonEmpty[AmendForeignPropertyPeriodSummaryRequestBody](data.body) match {
      case Nil          => NoValidationErrors
      case schemaErrors => List(schemaErrors)
    }
  }

  private def bodyFieldValidation: AmendForeignPropertyPeriodSummaryRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendForeignPropertyPeriodSummaryRequestBody]

    List(
      flattenErrors(
        List(
          body.foreignFhlEea.map(validateForeignFhlEea).getOrElse(NoValidationErrors),
          body.foreignNonFhlProperty
            .map(_.zipWithIndex.toList.flatMap {
              case (entry, i) => validateForeignProperty(entry, i)
            })
            .getOrElse(NoValidationErrors),
          body.foreignFhlEea.flatMap(_.expenses.map(validateForeignFhlEeaConsolidatedExpenses)).getOrElse(NoValidationErrors),
          body.foreignNonFhlProperty
            .map(_.zipWithIndex.toList.map {
              case (entry, i) =>
                entry.expenses.map(expenditure => validateForeignPropertyConsolidatedExpenses(expenditure, i)).getOrElse(NoValidationErrors)
            })
            .getOrElse(Nil)
            .flatten,
          duplicateCountryCodeValidation(body)
        ))
    )
  }

  private def validateForeignFhlEea(foreignFhlEea: AmendForeignFhlEea): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = foreignFhlEea.income.flatMap(_.rentAmount),
        path = "/foreignFhlEea/income/rentAmount"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenses.flatMap(_.premisesRunningCosts),
        path = "/foreignFhlEea/expenses/premisesRunningCosts"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenses.flatMap(_.repairsAndMaintenance),
        path = "/foreignFhlEea/expenses/repairsAndMaintenance"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenses.flatMap(_.financialCosts),
        path = "/foreignFhlEea/expenses/financialCosts"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenses.flatMap(_.professionalFees),
        path = "/foreignFhlEea/expenses/professionalFees"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenses.flatMap(_.costOfServices),
        path = "/foreignFhlEea/expenses/costOfServices"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenses.flatMap(_.travelCosts),
        path = "/foreignFhlEea/expenses/travelCosts"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenses.flatMap(_.other),
        path = "/foreignFhlEea/expenses/other"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenses.flatMap(_.consolidatedExpenses),
        path = "/foreignFhlEea/expenses/consolidatedExpenses"
      )
    ).flatten
  }

  private def validateForeignProperty(foreignPropertyEntry: AmendForeignNonFhlPropertyEntry, index: Int): List[MtdError] = {
    List(
      CountryCodeValidation.validate(
        field = foreignPropertyEntry.countryCode,
        path = s"/foreignNonFhlProperty/$index/countryCode"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.income.flatMap(_.rentIncome).flatMap(_.rentAmount),
        path = s"/foreignNonFhlProperty/$index/income/rentIncome/rentAmount"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.income.flatMap(_.premiumsOfLeaseGrant),
        path = s"/foreignNonFhlProperty/$index/income/premiumsOfLeaseGrant"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.income.flatMap(_.otherPropertyIncome),
        path = s"/foreignNonFhlProperty/$index/income/otherPropertyIncome"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.income.flatMap(_.foreignTaxPaidOrDeducted),
        path = s"/foreignNonFhlProperty/$index/income/foreignTaxPaidOrDeducted"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.income.flatMap(_.specialWithholdingTaxOrUkTaxPaid),
        path = s"/foreignNonFhlProperty/$index/income/specialWithholdingTaxOrUkTaxPaid"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.expenses.flatMap(_.premisesRunningCosts),
        path = s"/foreignNonFhlProperty/$index/expenses/premisesRunningCosts"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.expenses.flatMap(_.repairsAndMaintenance),
        path = s"/foreignNonFhlProperty/$index/expenses/repairsAndMaintenance"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.expenses.flatMap(_.financialCosts),
        path = s"/foreignNonFhlProperty/$index/expenses/financialCosts"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.expenses.flatMap(_.professionalFees),
        path = s"/foreignNonFhlProperty/$index/expenses/professionalFees"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.expenses.flatMap(_.costOfServices),
        path = s"/foreignNonFhlProperty/$index/expenses/costOfServices"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.expenses.flatMap(_.travelCosts),
        path = s"/foreignNonFhlProperty/$index/expenses/travelCosts"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.expenses.flatMap(_.residentialFinancialCost),
        path = s"/foreignNonFhlProperty/$index/expenses/residentialFinancialCost"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.expenses.flatMap(_.broughtFwdResidentialFinancialCost),
        path = s"/foreignNonFhlProperty/$index/expenses/broughtFwdResidentialFinancialCost"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.expenses.flatMap(_.other),
        path = s"/foreignNonFhlProperty/$index/expenses/other"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.expenses.flatMap(_.consolidatedExpenses),
        path = s"/foreignNonFhlProperty/$index/expenses/consolidatedExpenses"
      )
    ).flatten
  }

  private def validateForeignFhlEeaConsolidatedExpenses(expenses: AmendForeignFhlEeaExpenses): List[MtdError] = {
    ConsolidatedExpensesValidation.validate(
      expenses = expenses,
      path = "/foreignFhlEea/expenses"
    )
  }

  private def validateForeignPropertyConsolidatedExpenses(expenses: AmendForeignNonFhlPropertyExpenses, index: Int): List[MtdError] = {
    ConsolidatedExpensesValidation.validate(
      expenses = expenses,
      path = s"/foreignNonFhlProperty/$index/expenses"
    )
  }

  private def duplicateCountryCodeValidation(body: AmendForeignPropertyPeriodSummaryRequestBody): List[MtdError] = {
    body.foreignNonFhlProperty
      .map { entries =>
        entries.zipWithIndex
          .map {
            case (entry, idx) => (entry.countryCode, s"/foreignNonFhlProperty/$idx/countryCode")
          }
          .groupBy(_._1)
          .collect {
            case (code, codeAndPaths) if codeAndPaths.size >= 2 =>
              RuleDuplicateCountryCodeError.forDuplicatedCodesAndPaths(code, codeAndPaths.map(_._2))
          }
          .toList
      }
      .getOrElse(Nil)
  }

  override def validate(data: AmendForeignPropertyPeriodSummaryRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
