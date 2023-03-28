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
import api.controllers.requestParsers.validators.validations.{NinoValidation, TaxYearValidation}
import com.google.inject.Inject
import config.AppConfig
import api.controllers.requestParsers.validators.validations.JsonFormatValidation.validateAndCheckNonEmptyOrRead
import api.controllers.requestParsers.validators.validations.NumberValidation.{validateOptional => optionalNumber}
import api.models.errors.MtdError
import v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission.{
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData,
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody
}

import javax.inject.Singleton

@Singleton
class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator @Inject() (appConfig: AppConfig)
    extends Validator[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData] {

  lazy private val minTaxYear = appConfig.minimumTaxHistoric
  lazy private val maxTaxYear = appConfig.maximumTaxHistoric

  override def validate(data: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData): List[MtdError] = {
    (for {
      _    <- validatePathParams(data)
      body <- validateAndCheckNonEmptyOrRead[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody](data.body)
      _    <- validateBusinessRules(body)
    } yield ()).swap.getOrElse(Nil)
  }

  private def validatePathParams(data: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData): Either[List[MtdError], Unit] = {
    val errors =
      NinoValidation.validate(data.nino) ++
        TaxYearValidation.validateHistoric(minTaxYear, maxTaxYear, data.taxYear)

    errorsResult(errors)
  }

  private def validateBusinessRules(body: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody): Either[List[MtdError], Unit] = {
    val annualAdjustmentErrors = body.annualAdjustments
      .map { annualAdjustments =>
        import annualAdjustments._

        optionalNumber(lossBroughtForward, "/annualAdjustments/lossBroughtForward") ++
          optionalNumber(privateUseAdjustment, "/annualAdjustments/privateUseAdjustment") ++
          optionalNumber(balancingCharge, "/annualAdjustments/balancingCharge") ++
          optionalNumber(
            businessPremisesRenovationAllowanceBalancingCharges,
            "/annualAdjustments/businessPremisesRenovationAllowanceBalancingCharges")
      }
      .getOrElse(Nil)

    val annualAllowanceErrors = body.annualAllowances
      .map { annualAllowances =>
        import annualAllowances._

        optionalNumber(annualInvestmentAllowance, "/annualAllowances/annualInvestmentAllowance") ++
          optionalNumber(zeroEmissionGoodsVehicleAllowance, "/annualAllowances/zeroEmissionGoodsVehicleAllowance") ++
          optionalNumber(businessPremisesRenovationAllowance, "/annualAllowances/businessPremisesRenovationAllowance") ++
          optionalNumber(otherCapitalAllowance, "/annualAllowances/otherCapitalAllowance") ++
          optionalNumber(costOfReplacingDomesticGoods, "/annualAllowances/costOfReplacingDomesticGoods") ++
          optionalNumber(propertyIncomeAllowance, "/annualAllowances/propertyIncomeAllowance", max = 1000)
      }
      .getOrElse(Nil)

    errorsResult(annualAdjustmentErrors ++ annualAllowanceErrors)
  }

}
