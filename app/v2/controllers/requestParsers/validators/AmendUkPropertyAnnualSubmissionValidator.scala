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

import config.AppConfig
import javax.inject.{Inject, Singleton}
import v2.controllers.requestParsers.validators.validations._
import v2.models.errors._
import v2.models.request.amendUkPropertyAnnualSubmission.ukFhlProperty.{UkFhlProperty, UkFhlPropertyAllowances}
import v2.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty.{StructuredBuildingAllowance, UkNonFhlProperty, UkNonFhlPropertyAllowances}
import v2.models.request.amendUkPropertyAnnualSubmission.{AmendUkPropertyAnnualSubmissionRawData, AmendUkPropertyAnnualSubmissionRequestBody}

@Singleton
class AmendUkPropertyAnnualSubmissionValidator @Inject()(appConfig: AppConfig) extends Validator[AmendUkPropertyAnnualSubmissionRawData] {

  private lazy val minTaxYear = appConfig.minimumTaxV2Uk
  private val validationSet = List(parameterFormatValidation, bodyFormatValidation, bodyFieldValidation)

  private def parameterFormatValidation: AmendUkPropertyAnnualSubmissionRawData => List[List[MtdError]] =
    (data: AmendUkPropertyAnnualSubmissionRawData) => {
      List(
        NinoValidation.validate(data.nino),
        BusinessIdValidation.validate(data.businessId),
        TaxYearValidation.validate(minTaxYear, data.taxYear)
      )
    }

  private def bodyFormatValidation: AmendUkPropertyAnnualSubmissionRawData => List[List[MtdError]] = { data =>
    val schemeValidation = JsonFormatValidation.validate[AmendUkPropertyAnnualSubmissionRequestBody](data.body)

    val extraValidation =
      data.body.asOpt[AmendUkPropertyAnnualSubmissionRequestBody] match {
        case Some(body) if body.ukFhlProperty.isEmpty && body.ukNonFhlProperty.isEmpty => List(RuleIncorrectOrEmptyBodyError)
        case _                                                                         => NoValidationErrors
      }

    val emptyStructureValidation = JsonFormatValidation.validatedNestedEmpty(data.body)

    List(schemeValidation, extraValidation, emptyStructureValidation)
  }

  private def bodyFieldValidation: AmendUkPropertyAnnualSubmissionRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendUkPropertyAnnualSubmissionRequestBody]

    List(
      flattenErrors(
        List(body.ukFhlProperty.map(validateUkFhlProperty).getOrElse(NoValidationErrors),
        body.ukNonFhlProperty.map(validateukNonFhlProperty).getOrElse(NoValidationErrors),
          body.ukFhlProperty.flatMap(_.allowances).map(validateFhlAllowances).getOrElse(NoValidationErrors),
          body.ukNonFhlProperty.flatMap(_.allowances).map(validateNonFhlAllowances).getOrElse(NoValidationErrors),
          body.ukNonFhlProperty.flatMap(_.allowances.flatMap(_.structuredBuildingAllowance)).map(_.zipWithIndex.toList.flatMap {
            case (entry, i) => validateStructuredBuildingAllowance(entry, i)
          })
            .getOrElse(NoValidationErrors),
          body.ukNonFhlProperty.flatMap(_.allowances.flatMap(_.enhancedStructuredBuildingAllowance)).map(_.zipWithIndex.toList.flatMap {
            case (entry, i) => validateEnhancedStructuredBuildingAllowance(entry, i)
          })
            .getOrElse(NoValidationErrors)
        )
      )
    )
  }

  private def validateUkFhlProperty(ukFhlProperty: UkFhlProperty): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = ukFhlProperty.adjustments.flatMap(_.lossBroughtForward),
        path = "/ukFhlProperty/adjustments/lossBroughtForward"
      ),
      NumberValidation.validateOptional(
        field = ukFhlProperty.adjustments.flatMap(_.balancingCharge),
        path = "/ukFhlProperty/adjustments/balancingCharge"
      ),
      NumberValidation.validateOptional(
        field = ukFhlProperty.adjustments.flatMap(_.privateUseAdjustment),
        path = "/ukFhlProperty/adjustments/privateUseAdjustment"
      ),
      NumberValidation.validateOptional(
        field = ukFhlProperty.adjustments.flatMap(_.businessPremisesRenovationAllowanceBalancingCharges),
        path = "/ukFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges"
      ),
      NumberValidation.validateOptional(
        field = ukFhlProperty.allowances.flatMap(_.annualInvestmentAllowance),
        path = "/ukFhlProperty/allowances/annualInvestmentAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukFhlProperty.allowances.flatMap(_.businessPremisesRenovationAllowance),
        path = "/ukFhlProperty/allowances/businessPremisesRenovationAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukFhlProperty.allowances.flatMap(_.otherCapitalAllowance),
        path = "/ukFhlProperty/allowances/otherCapitalAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukFhlProperty.allowances.flatMap(_.electricChargePointAllowance),
        path = "/ukFhlProperty/allowances/electricChargePointAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukFhlProperty.allowances.flatMap(_.zeroEmissionsCarAllowance),
        path = "/ukFhlProperty/allowances/zeroEmissionsCarAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukFhlProperty.allowances.flatMap(_.propertyIncomeAllowance),
        path = "/ukFhlProperty/allowances/propertyIncomeAllowance"
      )
    ).flatten
  }

  private def validateukNonFhlProperty(ukNonFhlProperty: UkNonFhlProperty): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.adjustments.flatMap(_.lossBroughtForward),
        path = "/ukNonFhlProperty/adjustments/lossBroughtForward"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.adjustments.flatMap(_.balancingCharge),
        path = "/ukNonFhlProperty/adjustments/balancingCharge"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.adjustments.flatMap(_.privateUseAdjustment),
        path = "/ukNonFhlProperty/adjustments/privateUseAdjustment"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.adjustments.flatMap(_.businessPremisesRenovationAllowanceBalancingCharges),
        path = "/ukNonFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.annualInvestmentAllowance),
        path = "/ukNonFhlProperty/allowances/annualInvestmentAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.zeroEmissionGoodsVehicleAllowance),
        path = "/ukNonFhlProperty/allowances/zeroEmissionGoodsVehicleAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.businessPremisesRenovationAllowance),
        path = "/ukNonFhlProperty/allowances/businessPremisesRenovationAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.otherCapitalAllowance),
        path = "/ukNonFhlProperty/allowances/otherCapitalAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.costOfReplacingDomesticGoods),
        path = "/ukNonFhlProperty/allowances/costOfReplacingDomesticGoods"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.electricChargePointAllowance),
        path = "/ukNonFhlProperty/allowances/electricChargePointAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.zeroEmissionsCarAllowance),
        path = "/ukNonFhlProperty/allowances/zeroEmissionsCarAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.propertyIncomeAllowance),
        path = "/ukNonFhlProperty/allowances/propertyIncomeAllowance"
      )
    ).flatten
  }

  private def validateStructuredBuildingAllowance(buildingAllowance: StructuredBuildingAllowance, index: Int): List[MtdError] = {
    List(
    NumberValidation.validate(
      field = buildingAllowance.amount,
      path = s"/ukNonFhlProperty/allowances/structuredBuildingAllowance/$index/amount"
    ),
      DateValidation.validateOtherDate(
        field = buildingAllowance.firstYear.map(_.qualifyingDate),
        path = s"/ukNonFhlProperty/allowances/structuredBuildingAllowance/$index/firstYear/qualifyingDate"
      ),
      NumberValidation.validateOptional(
        field = buildingAllowance.firstYear.map(_.qualifyingAmountExpenditure),
        path = s"/ukNonFhlProperty/allowances/structuredBuildingAllowance/$index/firstYear/qualifyingAmountExpenditure"
      ),
      BuildingValidation.validate(
        body = buildingAllowance.building,
        path = s"/ukNonFhlProperty/allowances/structuredBuildingAllowance/$index/building"
      ),
      StringValidation.validateOptional(
        field = buildingAllowance.building.name,
        path = s"/ukNonFhlProperty/allowances/structuredBuildingAllowance/$index/building/name"
      ),
      StringValidation.validateOptional(
        field = buildingAllowance.building.number,
        path = s"/ukNonFhlProperty/allowances/structuredBuildingAllowance/$index/building/number"
      ),
      StringValidation.validate(
        field = buildingAllowance.building.postcode,
        path = s"/ukNonFhlProperty/allowances/structuredBuildingAllowance/$index/building/postcode"
      ),
    ).flatten
  }
  private def validateEnhancedStructuredBuildingAllowance(buildingAllowance: StructuredBuildingAllowance, index: Int): List[MtdError] = {
    List(
    NumberValidation.validate(
      field = buildingAllowance.amount,
      path = s"/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/$index/amount"
    ),
      DateValidation.validateOtherDate(
        field = buildingAllowance.firstYear.map(_.qualifyingDate),
        path = s"/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/$index/firstYear/qualifyingDate"
      ),
      NumberValidation.validateOptional(
        field = buildingAllowance.firstYear.map(_.qualifyingAmountExpenditure),
        path = s"/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/$index/firstYear/qualifyingAmountExpenditure"
      ),
      BuildingValidation.validate(
        body = buildingAllowance.building,
        path = s"/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/$index/building"
      ),
      StringValidation.validateOptional(
        field = buildingAllowance.building.name,
        path = s"/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/$index/building/name"
      ),
      StringValidation.validateOptional(
        field = buildingAllowance.building.number,
        path = s"/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/$index/building/number"
      ),
      StringValidation.validate(
        field = buildingAllowance.building.postcode,
        path = s"/ukNonFhlProperty/allowances/enhancedStructuredBuildingAllowance/$index/building/postcode"
      ),
    ).flatten
  }

  private def validateFhlAllowances(allowances: UkFhlPropertyAllowances) : List[MtdError] = {
    AllowancesValidation.validate(
      allowances = allowances,
      path = s"/ukFhlProperty/allowances"
    )
  }

  private def validateNonFhlAllowances(allowances: UkNonFhlPropertyAllowances) : List[MtdError] = {
    AllowancesValidation.validate(
      allowances = allowances,
      path = s"/ukNonFhlProperty/allowances"
    )
  }

  override def validate(data: AmendUkPropertyAnnualSubmissionRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}