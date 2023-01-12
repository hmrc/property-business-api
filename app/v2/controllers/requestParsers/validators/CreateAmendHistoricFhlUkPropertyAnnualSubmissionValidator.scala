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

import com.google.inject.Inject
import config.AppConfig
import v2.controllers.requestParsers.validators.validations.JsonFormatValidation.validateAndCheckNonEmptyOrRead
import v2.controllers.requestParsers.validators.validations.NumberValidation.{ validateOptional => optionalNumber }
import v2.controllers.requestParsers.validators.validations._
import v2.models.errors.MtdError
import v2.models.request.createAmendHistoricFhlUkPropertyAnnualSubmission.{
  CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData,
  CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody
}

import javax.inject.Singleton

@Singleton
class CreateAmendHistoricFhlUkPropertyAnnualSubmissionValidator @Inject()(appConfig: AppConfig)
    extends Validator[CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData] {

  lazy private val minTaxYear = appConfig.minimumTaxHistoric
  lazy private val maxTaxYear = appConfig.maximumTaxHistoric

  override def validate(data: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData): List[MtdError] = {
    (for {
      _    <- validatePathParams(data)
      body <- validateAndCheckNonEmptyOrRead[CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody](data.body)
      _    <- validateBusinessRules(body)
    } yield ()).swap.getOrElse(Nil)
  }

  private def validatePathParams(data: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData): Either[List[MtdError], Unit] = {
    val errors =
      NinoValidation.validate(data.nino) ++
        TaxYearValidation.validateHistoric(minTaxYear, maxTaxYear, data.taxYear)

    errorsResult(errors)
  }

  private def validateBusinessRules(body: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody): Either[List[MtdError], Unit] = {
    val annualAdjustmentErrors = body.annualAdjustments
      .map { annualAdjustments =>
        import annualAdjustments._

        optionalNumber(lossBroughtForward, "/annualAdjustments/lossBroughtForward") ++
          optionalNumber(privateUseAdjustment, "/annualAdjustments/privateUseAdjustment") ++
          optionalNumber(balancingCharge, "/annualAdjustments/balancingCharge") ++
          optionalNumber(businessPremisesRenovationAllowanceBalancingCharges,
                         "/annualAdjustments/businessPremisesRenovationAllowanceBalancingCharges")
      }
      .getOrElse(Nil)

    val annualAllowanceErrors = body.annualAllowances
      .map { annualAllowances =>
        import annualAllowances._

        optionalNumber(annualInvestmentAllowance, "/annualAllowances/annualInvestmentAllowance") ++
          optionalNumber(propertyIncomeAllowance, "/annualAllowances/propertyIncomeAllowance", max = 1000) ++
          optionalNumber(otherCapitalAllowance, "/annualAllowances/otherCapitalAllowance") ++
          optionalNumber(businessPremisesRenovationAllowance, "/annualAllowances/businessPremisesRenovationAllowance")

      }
      .getOrElse(Nil)

    errorsResult(annualAdjustmentErrors ++ annualAllowanceErrors)
  }
}
