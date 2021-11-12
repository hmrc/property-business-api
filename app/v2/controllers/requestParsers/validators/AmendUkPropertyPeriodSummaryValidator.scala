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

package v2.controllers.requestParsers.validators

import com.google.inject.Inject
import config.AppConfig
import v2.controllers.requestParsers.validators.validations._
import v2.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import v2.models.request.amendUkPropertyPeriodSummary._
import v2.models.request.common.ukFhlProperty.UkFhlProperty
import v2.models.request.common.ukNonFhlProperty.UkNonFhlProperty

import javax.inject.Singleton

@Singleton
class AmendUkPropertyPeriodSummaryValidator @Inject()(appConfig: AppConfig) extends Validator[AmendUkPropertyPeriodSummaryRawData] {

  private lazy val minTaxYear = appConfig.minimumTaxV2Uk
  private val validationSet   = List(parameterFormatValidation, bodyFormatValidation, bodyFieldFormatValidation)

  private def parameterFormatValidation: AmendUkPropertyPeriodSummaryRawData => List[List[MtdError]] =
    (data: AmendUkPropertyPeriodSummaryRawData) => {
      List(
        NinoValidation.validate(data.nino),
        BusinessIdValidation.validate(data.businessId),
        TaxYearValidation.validate(minTaxYear, data.taxYear),
        SubmissionIdValidation.validate(data.submissionId)

      )
    }

  private def bodyFormatValidation: AmendUkPropertyPeriodSummaryRawData => List[List[MtdError]] = { data =>
    val schemaValidation = JsonFormatValidation.validate[AmendUkPropertyPeriodSummaryRequestBody](data.body)

    val extraValidation =
      data.body.asOpt[AmendUkPropertyPeriodSummaryRequestBody] match {
        case Some(body) if body.ukNonFhlProperty.isEmpty && body.ukFhlProperty.isEmpty => List(RuleIncorrectOrEmptyBodyError)
        case _                                                                         => NoValidationErrors
      }

    val emptyStructureValidation = JsonFormatValidation.validatedNestedEmpty(data.body)

    List(schemaValidation, extraValidation, emptyStructureValidation)
  }

  private def bodyFieldFormatValidation: AmendUkPropertyPeriodSummaryRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendUkPropertyPeriodSummaryRequestBody]

    val pathErrors = List(
      flattenErrors(
        List(
          body.ukFhlProperty.map(validateFhlMonetaryValues).getOrElse(NoValidationErrors),
          body.ukNonFhlProperty.map(validateNonFhlMonetaryValues).getOrElse(NoValidationErrors),
          body.ukFhlProperty.flatMap(_.expenses.map(ConsolidatedExpensesValidation.validate(_,"/ukFhlProperty/expenses"))).getOrElse(NoValidationErrors),
          body.ukNonFhlProperty.flatMap(_.expenses.map(ConsolidatedExpensesValidation.validate(_,"/ukNonFhlProperty/expenses"))).getOrElse(NoValidationErrors),
        )))

    pathErrors

  }

  private def validateFhlMonetaryValues(property: UkFhlProperty): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = property.income.flatMap(_.periodAmount),
        path = "/ukFhlProperty/income/periodAmount"
      ),
      NumberValidation.validateOptional(
        field = property.income.flatMap(_.taxDeducted),
        path = "/ukFhlProperty/income/taxDeducted"
      ),
      NumberValidation.validateOptional(
        field = property.income.flatMap(_.rentARoom.flatMap(_.rentsReceived)),
        path = "/ukFhlProperty/income/rentARoom/rentsReceived"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.premisesRunningCosts),
        path = "/ukFhlProperty/expenses/premisesRunningCosts"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.repairsAndMaintenance),
        path = "/ukFhlProperty/expenses/repairsAndMaintenance"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.financialCosts),
        path = "/ukFhlProperty/expenses/financialCosts"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.professionalFees),
        path = "/ukFhlProperty/expenses/professionalFees"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.costOfServices),
        path = "/ukFhlProperty/expenses/costOfServices"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.other),
        path = "/ukFhlProperty/expenses/other"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.consolidatedExpenses),
        path = "/ukFhlProperty/expenses/consolidatedExpenses"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.travelCosts),
        path = "/ukFhlProperty/expenses/travelCosts"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.rentARoom.flatMap(_.amountClaimed)),
        path = "/ukFhlProperty/expenses/rentARoom/amountClaimed"
      ),
    ).flatten
  }

  private def validateNonFhlMonetaryValues(property: UkNonFhlProperty): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = property.income.flatMap(_.premiumsOfLeaseGrant),
        path = "/ukNonFhlProperty/income/premiumsOfLeaseGrant"
      ),
      NumberValidation.validateOptional(
        field = property.income.flatMap(_.reversePremiums),
        path = "/ukNonFhlProperty/income/reversePremiums"
      ),
      NumberValidation.validateOptional(
        field = property.income.flatMap(_.periodAmount),
        path = "/ukNonFhlProperty/income/periodAmount"
      ),
      NumberValidation.validateOptional(
        field = property.income.flatMap(_.taxDeducted),
        path = "/ukNonFhlProperty/income/taxDeducted"
      ),
      NumberValidation.validateOptional(
        field = property.income.flatMap(_.otherIncome),
        path = "/ukNonFhlProperty/income/otherIncome"
      ),
      NumberValidation.validateOptional(
        field = property.income.flatMap(_.rentARoom.flatMap(_.rentsReceived)),
        path = "/ukNonFhlProperty/income/rentARoom/rentsReceived"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.premisesRunningCosts),
        path = "/ukNonFhlProperty/expenses/premisesRunningCosts"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.repairsAndMaintenance),
        path = "/ukNonFhlProperty/expenses/repairsAndMaintenance"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.financialCosts),
        path = "/ukNonFhlProperty/expenses/financialCosts"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.professionalFees),
        path = "/ukNonFhlProperty/expenses/professionalFees"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.costOfServices),
        path = "/ukNonFhlProperty/expenses/costOfServices"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.other),
        path = "/ukNonFhlProperty/expenses/other"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.residentialFinancialCost),
        path = "/ukNonFhlProperty/expenses/residentialFinancialCost"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.consolidatedExpenses),
        path = "/ukNonFhlProperty/expenses/consolidatedExpenses"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.travelCosts),
        path = "/ukNonFhlProperty/expenses/travelCosts"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.residentialFinancialCostsCarriedForward),
        path = "/ukNonFhlProperty/expenses/residentialFinancialCostsCarriedForward"
      ),
      NumberValidation.validateOptional(
        field = property.expenses.flatMap(_.rentARoom.flatMap(_.amountClaimed)),
        path = "/ukNonFhlProperty/expenses/rentARoom/amountClaimed"
      ),
    ).flatten
  }

 // private def validateUkPropertyConsolidatedExpenses(expenses: UkFhlPropertyExpenses): List[MtdError] = {
 //   ConsolidatedExpensesValidation.validate(
 //     expenses = expenses,
 //     path = s"/ukFhlProperty/expenses"
 //   )
 // }

 // private def validateUkNonFhlPropertyConsolidatedExpenses(expenses: UkFhlPropertyExpenses): List[MtdError] = {
 //   ConsolidatedExpensesValidation.validate(
 //     expenses = expenses,
 //     path = s"/ukNonFhlProperty/expenses"
  //  )
  //}

  override def validate(data: AmendUkPropertyPeriodSummaryRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
