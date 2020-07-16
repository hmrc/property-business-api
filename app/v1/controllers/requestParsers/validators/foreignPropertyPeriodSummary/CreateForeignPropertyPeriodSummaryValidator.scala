/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.foreignPropertyPeriodSummary

import v1.controllers.requestParsers.validators.Validator
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import v1.models.request.foreignPropertyPeriodSummary.common.foreignFhlEea.{ForeignFhlEea, ForeignFhlEeaExpenditure}
import v1.models.request.foreignPropertyPeriodSummary.common.foreignPropertyEntry.{ForeignPropertyEntry, ForeignPropertyExpenditure}
import v1.models.request.foreignPropertyPeriodSummary.createForeignPropertyPeriodSummary.{CreateForeignPropertyPeriodSummaryRawData, CreateForeignPropertyPeriodSummaryRequestBody}

class CreateForeignPropertyPeriodSummaryValidator extends Validator[CreateForeignPropertyPeriodSummaryRawData] {

  private val validationSet = List(parameterFormatValidation, bodyFormatValidation, bodyFieldFormatValidation, dateRangeValidation)

  private def parameterFormatValidation: CreateForeignPropertyPeriodSummaryRawData => List[List[MtdError]] = (data: CreateForeignPropertyPeriodSummaryRawData) => {
    List(
      NinoValidation.validate(data.nino),
      BusinessIdValidation.validate(data.businessId)
    )
  }

  private def bodyFormatValidation: CreateForeignPropertyPeriodSummaryRawData => List[List[MtdError]] = { data =>
    val baseValidation = List(JsonFormatValidation.validate[CreateForeignPropertyPeriodSummaryRequestBody](data.body, RuleIncorrectOrEmptyBodyError))

    val extraValidation: List[List[MtdError]] = {
      data.body.asOpt[CreateForeignPropertyPeriodSummaryRequestBody].map(_.isEmpty).map {
        case true => List(List(RuleIncorrectOrEmptyBodyError))
        case false => NoValidationErrors
      }.getOrElse(NoValidationErrors)
    }

    baseValidation ++ extraValidation
  }

  private def bodyFieldFormatValidation: CreateForeignPropertyPeriodSummaryRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[CreateForeignPropertyPeriodSummaryRequestBody]

    val regularErrors = List(
      DateValidation.validate(body.fromDate, isFromDate = true),
      DateValidation.validate(body.toDate, isFromDate = false)
    )

    val pathErrors = List(flattenErrors(List(
      body.foreignFhlEea.map(validateForeignFhlEea).getOrElse(NoValidationErrors),
      body.foreignProperty.map(_.zipWithIndex.toList.flatMap {
        case (entry, i) => validateForeignProperty(entry, i)
      }).getOrElse(NoValidationErrors),
      body.foreignFhlEea.flatMap(_.expenditure.map(validateForeignFhlEeaConsolidatedExpenses)).getOrElse(NoValidationErrors),
      body.foreignProperty.map(
        _.toList.zipWithIndex.map {
          case (entry, i) =>
            entry.expenditure.map(expenditure => validateForeignPropertyConsolidatedExpenses(expenditure, i)).getOrElse(NoValidationErrors)
        }
      ).getOrElse(Nil).flatten
    )))

    regularErrors ++ pathErrors
  }

  private def validateForeignFhlEea(foreignFhlEea: ForeignFhlEea): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = Some(foreignFhlEea.income.rentAmount),
        path = "/foreignFhlEea/income/rentAmount"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.income.taxDeducted,
        path = "/foreignFhlEea/income/taxDeducted"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenditure.flatMap(_.premisesRunningCosts),
        path = "/foreignFhlEea/expenditure/premisesRunningCosts"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenditure.flatMap(_.repairsAndMaintenance),
        path = "/foreignFhlEea/expenditure/repairsAndMaintenance"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenditure.flatMap(_.financialCosts),
        path = "/foreignFhlEea/expenditure/financialCosts"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenditure.flatMap(_.professionalFees),
        path = "/foreignFhlEea/expenditure/professionalFees"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenditure.flatMap(_.costsOfServices),
        path = "/foreignFhlEea/expenditure/costsOfServices"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenditure.flatMap(_.travelCosts),
        path = "/foreignFhlEea/expenditure/travelCosts"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenditure.flatMap(_.other),
        path = "/foreignFhlEea/expenditure/other"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.expenditure.flatMap(_.consolidatedExpenses),
        path = "/foreignFhlEea/expenditure/consolidatedExpenses"
      )
    ).flatten
  }

  private def validateForeignProperty(foreignProperty: ForeignPropertyEntry, index: Int): List[MtdError] = {
    List(
      CountryCodeValidation.validate(
        field = foreignProperty.countryCode,
        path = s"/foreignProperty/$index/countryCode"
      ),
      NumberValidation.validateOptional(
        field = Some(foreignProperty.income.rentIncome.rentAmount),
        path = s"/foreignProperty/$index/income/rentIncome/rentAmount"
      ),
      NumberValidation.validateOptional(
        field = Some(foreignProperty.income.rentIncome.taxDeducted),
        path = s"/foreignProperty/$index/income/rentIncome/taxDeducted"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.income.premiumOfLeaseGrant,
        path = s"/foreignProperty/$index/income/premiumOfLeaseGrant"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.income.otherPropertyIncome,
        path = s"/foreignProperty/$index/income/otherPropertyIncome"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.income.foreignTaxTakenOff,
        path = s"/foreignProperty/$index/income/foreignTaxTakenOff"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.income.specialWithholdingTaxOrUKTaxPaid,
        path = s"/foreignProperty/$index/income/specialWithholdingTaxOrUKTaxPaid"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.expenditure.flatMap(_.premisesRunningCosts),
        path = s"/foreignProperty/$index/expenditure/premisesRunningCosts"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.expenditure.flatMap(_.repairsAndMaintenance),
        path = s"/foreignProperty/$index/expenditure/repairsAndMaintenance"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.expenditure.flatMap(_.financialCosts),
        path = s"/foreignProperty/$index/expenditure/financialCosts"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.expenditure.flatMap(_.professionalFees),
        path = s"/foreignProperty/$index/expenditure/professionalFees"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.expenditure.flatMap(_.costsOfServices),
        path = s"/foreignProperty/$index/expenditure/costsOfServices"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.expenditure.flatMap(_.travelCosts),
        path = s"/foreignProperty/$index/expenditure/travelCosts"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.expenditure.flatMap(_.residentialFinancialCost),
        path = s"/foreignProperty/$index/expenditure/residentialFinancialCost"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.expenditure.flatMap(_.broughtFwdResidentialFinancialCost),
        path = s"/foreignProperty/$index/expenditure/broughtFwdResidentialFinancialCost"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.expenditure.flatMap(_.other),
        path = s"/foreignProperty/$index/expenditure/other"
      ),
      NumberValidation.validateOptional(
        field = foreignProperty.expenditure.flatMap(_.consolidatedExpenses),
        path = s"/foreignProperty/$index/expenditure/consolidatedExpenses"
      )
    ).flatten
  }

  private def validateForeignFhlEeaConsolidatedExpenses(expenditure: ForeignFhlEeaExpenditure): List[MtdError] = {
    ConsolidatedExpensesValidation.validate(
      expenditure = expenditure,
      path = s"/foreignFhlEea/expenditure"
    )
  }

  private def validateForeignPropertyConsolidatedExpenses(expenditure: ForeignPropertyExpenditure, index: Int): List[MtdError] = {
    ConsolidatedExpensesValidation.validate(
      expenditure = expenditure,
      path = s"/foreignProperty/$index/expenditure"
    )
  }

  private def dateRangeValidation: CreateForeignPropertyPeriodSummaryRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[CreateForeignPropertyPeriodSummaryRequestBody]

    List(ToDateBeforeFromDateValidation.validate(body.fromDate, body.toDate))
  }

  override def validate(data: CreateForeignPropertyPeriodSummaryRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}