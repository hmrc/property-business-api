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
import v2.models.request.amendUkPropertyAnnualSubmission.ukFhlProperty.{UkFhlProperty, UkFhlPropertyAdjustments, UkFhlPropertyAllowances}
import v2.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty.{FirstYear, StructuredBuildingAllowance, UkNonFhlProperty, UkNonFhlPropertyAllowances}
import v2.models.request.amendUkPropertyAnnualSubmission.{AmendUkPropertyAnnualSubmissionRawData, AmendUkPropertyAnnualSubmissionRequestBody}

@Singleton
class AmendUkPropertyAnnualSubmissionValidator @Inject()(appConfig: AppConfig) extends Validator[AmendUkPropertyAnnualSubmissionRawData] {

  private lazy val minTaxYear = appConfig.minimumTaxV2Uk
  private val validationSet = List(parameterFormatValidation, bodyFieldValidation)

  private def parameterFormatValidation: AmendUkPropertyAnnualSubmissionRawData => List[List[MtdError]] =
    (data: AmendUkPropertyAnnualSubmissionRawData) => {
      List(
        NinoValidation.validate(data.nino),
        BusinessIdValidation.validate(data.businessId),
        TaxYearValidation.validate(minTaxYear, data.taxYear)
      )
    }

//  private def bodyFormatValidation: AmendUkPropertyAnnualSubmissionRawData => List[List[MtdError]] = { data =>
//    List(JsonFormatValidation.validate[AmendUkPropertyAnnualSubmissionRequestBody](data.body)) ++
//      List(JsonFormatValidation.validatedNestedEmpty(data.body))
//  }


  private def bodyFieldValidation: AmendUkPropertyAnnualSubmissionRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendUkPropertyAnnualSubmissionRequestBody]


    List(
      flattenErrors(
        List(body.ukFhlProperty.map(validateUkFhlProperty).getOrElse(NoValidationErrors),
        body.ukNonFhlProperty.map(validateukNonFhlProperty).getOrElse(NoValidationErrors),
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
        path = "/ukFhlProperty/adjustments/lossBroughtForward"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.adjustments.flatMap(_.balancingCharge),
        path = "/ukFhlProperty/adjustments/balancingCharge"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.adjustments.flatMap(_.privateUseAdjustment),
        path = "/ukFhlProperty/adjustments/privateUseAdjustment"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.adjustments.flatMap(_.businessPremisesRenovationAllowanceBalancingCharges),
        path = "/ukFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.annualInvestmentAllowance),
        path = "/ukFhlProperty/allowances/annualInvestmentAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.zeroEmissionGoodsVehicleAllowance),
        path = "/ukFhlProperty/allowances/zeroEmissionGoodsVehicleAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.businessPremisesRenovationAllowance),
        path = "/ukFhlProperty/allowances/businessPremisesRenovationAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.otherCapitalAllowance),
        path = "/ukFhlProperty/allowances/otherCapitalAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.costOfReplacingDomesticGoods),
        path = "/ukFhlProperty/allowances/costOfReplacingDomesticGoods"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.electricChargePointAllowance),
        path = "/ukFhlProperty/allowances/electricChargePointAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.zeroEmissionsCarAllowance),
        path = "/ukFhlProperty/allowances/zeroEmissionsCarAllowance"
      ),
      NumberValidation.validateOptional(
        field = ukNonFhlProperty.allowances.flatMap(_.propertyIncomeAllowance),
        path = "/ukFhlProperty/allowances/propertyIncomeAllowance"
      ),
//      NumberValidation.validateOptional(
//        field = ukNonFhlProperty.allowances.flatMap(_.structuredBuildingAllowance),
//        path = "/ukFhlProperty/allowances/structuredBuildingAllowance/amount"
//      ),
//      DateValidation.validateOtherDate(
//        field = ukNonFhlProperty.allowances.flatMap(_.structuredBuildingAllowance.firstYear.qualifyingDate),
//        path = "/ukFhlProperty/allowances/structuredBuildingAllowance/firstYear/qualifyingDate"
//      ),
//      NumberValidation.validateOptional(
//        field = ukNonFhlProperty.allowances.flatMap(_.structuredBuildingAllowance.firstYear.qualifyingAmountExpenditure),
//        path = "/ukFhlProperty/allowances/structuredBuildingAllowance/firstYear/qualifyingAmountExpenditure"
//      ),
//      StringValidation.validate(
//        field = ukNonFhlProperty.allowances.flatMap(_.structuredBuildingAllowance.building.name),
//        path = "/ukFhlProperty/allowances/structuredBuildingAllowance/building/name"
//      ),
//      StringValidation.validate(
//        field = ukNonFhlProperty.allowances.flatMap(_.structuredBuildingAllowance.building.number),
//        path = "/ukFhlProperty/allowances/structuredBuildingAllowance/building/number"
//      ),
//      StringValidation.validate(
//        field = ukNonFhlProperty.allowances.flatMap(_.structuredBuildingAllowance.building.postcode),
//        path = "/ukFhlProperty/allowances/structuredBuildingAllowance/building/postcode"
//      ),
//      NumberValidation.validateOptional(
//        field = ukNonFhlProperty.allowances.flatMap(_.enhancedStructuredBuildingAllowance.amount),
//        path = "/ukFhlProperty/allowances/enhancedStructuredBuildingAllowance/amount"
//      ),
//      DateValidation.validateOtherDate(
//        field = ukNonFhlProperty.allowances.flatMap(_.enhancedStructuredBuildingAllowance.firstYear.qualifyingDate),
//        path = "/ukFhlProperty/allowances/enhancedStructuredBuildingAllowance/firstYear/qualifyingDate"
//      ),
//      NumberValidation.validateOptional(
//        field = ukNonFhlProperty.allowances.flatMap(_.enhancedStructuredBuildingAllowance.firstYear.qualifyingAmountExpenditure),
//        path = "/ukFhlProperty/allowances/enhancedStructuredBuildingAllowance/firstYear/qualifyingAmountExpenditure"
//      ),
//      StringValidation.validate(
//        field = ukNonFhlProperty.allowances.flatMap(_.enhancedStructuredBuildingAllowance.building.name),
//        path = "/ukFhlProperty/allowances/enhancedStructuredBuildingAllowance/building/name"
//      ),
//      StringValidation.validate(
//        field = ukNonFhlProperty.allowances.flatMap(_.enhancedStructuredBuildingAllowance.building.number),
//        path = "/ukFhlProperty/allowances/enhancedStructuredBuildingAllowance/building/number"
//      ),
//      StringValidation.validate(
//        field = ukNonFhlProperty.allowances.flatMap(_.enhancedStructuredBuildingAllowance.building.postcode),
//        path = "/ukFhlProperty/allowances/enhancedStructuredBuildingAllowance/building/postcode"
//      )
    ).flatten
  }

  private def validateStructuredBuildingAllowance(buildingAllowance: StructuredBuildingAllowance, index: Int): List[MtdError] = {
    List(
    NumberValidation.validate(
      field = buildingAllowance.amount,
      path = "/ukFhlProperty/allowances/structuredBuildingAllowance/amount"
    ),
      DateValidation.validateOtherDate(
        field = buildingAllowance.firstYear.map(_.qualifyingDate).get,
        path = s"/ukFhlProperty/allowances/structuredBuildingAllowance/$index/firstYear/qualifyingDate"
      )
    ).flatten
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

  override def validate(data: AmendUkPropertyAnnualSubmissionRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}