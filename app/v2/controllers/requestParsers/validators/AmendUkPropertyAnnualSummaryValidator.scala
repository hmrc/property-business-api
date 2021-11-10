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
class AmendUkPropertyAnnualSummaryValidator @Inject()(appConfig: AppConfig) extends Validator[AmendUkPropertyAnnualSubmissionRawData] {

  private lazy val minTaxYear = appConfig.minimumTaxV2Uk
  private val validationSet   = List(parameterFormatValidation)

  private def parameterFormatValidation: AmendUkPropertyAnnualSubmissionRawData => List[List[MtdError]] =
    (data: AmendUkPropertyAnnualSubmissionRawData) => {
      List(
        NinoValidation.validate(data.nino),
        BusinessIdValidation.validate(data.businessId),
        TaxYearValidation.validate(minTaxYear, data.taxYear)
      )
    }

  private def bodyFormatValidation: AmendUkPropertyAnnualSubmissionRawData => List[List[MtdError]] = { data =>
    val baseValidation = List(
      JsonFormatValidation.validate[AmendUkPropertyAnnualSubmissionRequestBody](data.body, RuleIncorrectOrEmptyBodyError))

    val extraValidation: List[List[MtdError]] = {
      data.body
        .asOpt[AmendUkPropertyAnnualSubmissionRequestBody]
        .map(_.isEmpty)
        .map {
          case true  => List(List(RuleIncorrectOrEmptyBodyError))
          case false => NoValidationErrors
        }
        .getOrElse(NoValidationErrors)
    }

    baseValidation ++ extraValidation
  }

  private def bodyFieldValidation: AmendUkPropertyAnnualSubmissionRawData => List[List[MtdError]] { data =>
  val body = data.body.as[AmendUkPropertyAnnualSubmissionRequestBody]

  List(
    flattenErrors(
      List(
        body.ukFhlProperty.map(validateUkFhlProperty).getOrElse(NoValidationErrors),
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

    )
  }

}
