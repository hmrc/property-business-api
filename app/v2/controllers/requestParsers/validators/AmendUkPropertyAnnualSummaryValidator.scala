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

@Singleton
class AmendUkPropertyAnnualSummaryValidator @Inject()(appConfig: AppConfig) extends Validator[AmendUkPropertyAnnualSummaryRawData] {

  private lazy val minTaxYear = appConfig.minimumTaxV2Uk
  private val validationSet = List(parameterFormatValidation)

  private def parameterFormatValidation: AmendUkPropertyAnnualSummaryRawData => List[List[MtdError]] =
    (data: AmendUkPropertyAnnualSummaryRawData) => {
      List(
        NinoValidation.validate(data.nino),
        BusinessIdValidation.validate(data.businessId),
        TaxYearValidation.validate(minTaxYear, data.taxYear)
      )
    }

  private def bodyFormatValidation: AmendUkPropertyAnnualSummaryRawData => List[List[MtdError]] = { data =>
    List(JsonFormatValidation.validate[AmendUkPropertyAnnualSummaryRequestBody](data.body)) ++
      List(JsonFormatValidation.validatedNestedEmpty(data.body))
  }

  private def bodyFieldValidation: AmendUkPropertyAnnualSummaryRawData => List[List[MtdError]] {data =>
    val body = data.body.as[AmendUkPropertyAnnualSummaryRequestBody]

    List (
    flattenErrors (
    List (
    body.ukFhlProperty.map (validateUkFhlProperty).getOrElse (NoValidationErrors),
    body.ukFhlProperty

    )
    )
    )
  }

  private def validateUkFhlProperty(ukFhlProperty: UkFhlProperty): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = Some(ukFhlProperty.adjustments.lossBroughtForward),
        path = "/ukFhlProperty/adjustments/lossBroughtForward"
      ),
      NumberValidation.validateOptional(
        field = Some(ukFhlProperty.adjustments.balancingCharge),
        path = "/ukFhlProperty/adjustments/balancingCharge"
      ),
      NumberValidation.validateOptional(
        field = Some(ukFhlProperty.adjustments.privateUseAdjustment),
        path = "/ukFhlProperty/adjustments/privateUseAdjustment"
      ),
      NumberValidation.validateOptional(
        field = Some(ukFhlProperty.adjustments.businessPremisesRenovationAllowanceBalancingCharges),
        path = "/ukFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges"
      ),
      NumberValidation.validateOptional(
        field = Some(ukFhlProperty.allowances.annualInvestmentAllowance),
        path = "/ukFhlProperty/allowances/annualInvestmentAllowance"
      ),
      NumberValidation.validateOptional(
        field = Some(ukFhlProperty.allowances.businessPremisesRenovationAllowance),
        path = "/ukFhlProperty/allowances/businessPremisesRenovationAllowance"
      ),
      NumberValidation.validateOptional(
        field = Some(ukFhlProperty.allowances.otherCapitalAllowance),
        path = "/ukFhlProperty/allowances/otherCapitalAllowance"
      ),
      NumberValidation.validateOptional(
        field = Some(ukFhlProperty.allowances.electricChargePointAllowance),
        path = "/ukFhlProperty/allowances/electricChargePointAllowance"
      ),
      NumberValidation.validateOptional(
        field = Some(ukFhlProperty.allowances.zeroEmissionsCarAllowance),
        path = "/ukFhlProperty/allowances/zeroEmissionsCarAllowance"
      ),
      NumberValidation.validateOptional(
        field = Some(ukFhlProperty.allowances.propertyIncomeAllowance),
        path = "/ukFhlProperty/allowances/propertyIncomeAllowance"
      )
    )
  }

  private def validateukNonFhlProperty(ukNonFhlProperty: UkNonFhlProperty): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.adjustments.lossBroughtForward),
        path = "/ukFhlProperty/adjustments/lossBroughtForward"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.adjustments.balancingCharge),
        path = "/ukFhlProperty/adjustments/balancingCharge"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.adjustments.privateUseAdjustment),
        path = "/ukFhlProperty/adjustments/privateUseAdjustment"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.adjustments.businessPremisesRenovationAllowanceBalancingCharges),
        path = "/ukFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.allowances.annualInvestmentAllowance),
        path = "/ukFhlProperty/allowances/annualInvestmentAllowance"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.allowances.zeroEmissionGoodsVehicleAllowance),
        path = "/ukFhlProperty/allowances/zeroEmissionGoodsVehicleAllowance"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.allowances.businessPremisesRenovationAllowance),
        path = "/ukFhlProperty/allowances/businessPremisesRenovationAllowance"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.allowances.otherCapitalAllowance),
        path = "/ukFhlProperty/allowances/otherCapitalAllowance"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.allowances.costOfReplacingDomesticGoods),
        path = "/ukFhlProperty/allowances/costOfReplacingDomesticGoods"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.allowances.electricChargePointAllowance),
        path = "/ukFhlProperty/allowances/electricChargePointAllowance"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.allowances.zeroEmissionsCarAllowance),
        path = "/ukFhlProperty/allowances/zeroEmissionsCarAllowance"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.allowances.propertyIncomeAllowance),
        path = "/ukFhlProperty/allowances/propertyIncomeAllowance"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.allowances.structuredBuildingAllowance.amount),
        path = "/ukFhlProperty/allowances/structuredBuildingAllowance/amount"
      ),
      DateValidation.validateOtherDate(
        field = Some(ukNonFhlProperty.allowances.structuredBuildingAllowance.firstYear.qualifyingDate),
        path = "/ukFhlProperty/allowances/structuredBuildingAllowance/firstYear/qualifyingDate"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.allowances.structuredBuildingAllowance.firstYear.qualifyingAmountExpenditure),
        path = "/ukFhlProperty/allowances/structuredBuildingAllowance/firstYear/qualifyingAmountExpenditure"
      ),
      StringValidation.validate(
        field = Some(ukNonFhlProperty.allowances.structuredBuildingAllowance.building.name),
        path = "/ukFhlProperty/allowances/structuredBuildingAllowance/building/name"
      ),
      StringValidation.validate(
        field = Some(ukNonFhlProperty.allowances.structuredBuildingAllowance.building.number),
        path = "/ukFhlProperty/allowances/structuredBuildingAllowance/building/number"
      ),
      StringValidation.validate(
        field = Some(ukNonFhlProperty.allowances.structuredBuildingAllowance.building.postcode),
        path = "/ukFhlProperty/allowances/structuredBuildingAllowance/building/postcode"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.allowances.enhancedStructuredBuildingAllowance.amount),
        path = "/ukFhlProperty/allowances/enhancedStructuredBuildingAllowance/amount"
      ),
      DateValidation.validateOtherDate(
        field = Some(ukNonFhlProperty.allowances.enhancedStructuredBuildingAllowance.firstYear.qualifyingDate),
        path = "/ukFhlProperty/allowances/enhancedStructuredBuildingAllowance/firstYear/qualifyingDate"
      ),
      NumberValidation.validateOptional(
        field = Some(ukNonFhlProperty.allowances.enhancedStructuredBuildingAllowance.firstYear.qualifyingAmountExpenditure),
        path = "/ukFhlProperty/allowances/enhancedStructuredBuildingAllowance/firstYear/qualifyingAmountExpenditure"
      ),
      StringValidation.validate(
        field = Some(ukNonFhlProperty.allowances.enhancedStructuredBuildingAllowance.building.name),
        path = "/ukFhlProperty/allowances/enhancedStructuredBuildingAllowance/building/name"
      ),
      StringValidation.validate(
        field = Some(ukNonFhlProperty.allowances.enhancedStructuredBuildingAllowance.building.number),
        path = "/ukFhlProperty/allowances/enhancedStructuredBuildingAllowance/building/number"
      ),
      StringValidation.validate(
        field = Some(ukNonFhlProperty.allowances.enhancedStructuredBuildingAllowance.building.postcode),
        path = "/ukFhlProperty/allowances/enhancedStructuredBuildingAllowance/building/postcode"
      )
    )
  }



  private def validateFhlAllowances(allowances: UkFhlPropertyAllowances) : List[MtdError] = {
    AllowancesValidation.validate(
      allowances = allowances,
      path = s"ukFhlProperty/allowances"
    )
  }

  private def validateFhlAllowances(allowances: UkNonFhlPropertyAllowances) : List[MtdError] = {
    AllowancesValidation.validate(
      allowances = allowances,
      path = s"ukNonFhlProperty/allowances"
    )
  }

  override def validate(data: AmendUkPropertyAnnualSummaryRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}