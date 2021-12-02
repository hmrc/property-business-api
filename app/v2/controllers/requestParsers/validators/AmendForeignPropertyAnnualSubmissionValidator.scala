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
import v2.controllers.requestParsers.validators.validations._
import v2.models.errors._
import v2.models.request.amendForeignPropertyAnnualSubmission.foreignFhlEea.ForeignFhlEea
import v2.models.request.amendForeignPropertyAnnualSubmission.foreignNonFhl.ForeignNonFhlEntry
import v2.models.request.amendForeignPropertyAnnualSubmission.{
  AmendForeignPropertyAnnualSubmissionRawData,
  AmendForeignPropertyAnnualSubmissionRequestBody
}

import javax.inject.{ Inject, Singleton }

@Singleton
class AmendForeignPropertyAnnualSubmissionValidator @Inject()(appConfig: AppConfig) extends Validator[AmendForeignPropertyAnnualSubmissionRawData] {

  private lazy val minTaxYear = appConfig.minimumTaxV2Foreign
  private val validationSet   = List(parameterFormatValidation, bodyFormatValidation, bodyFieldValidation)

  private def parameterFormatValidation: AmendForeignPropertyAnnualSubmissionRawData => List[List[MtdError]] =
    (data: AmendForeignPropertyAnnualSubmissionRawData) => {
      List(
        NinoValidation.validate(data.nino),
        BusinessIdValidation.validate(data.businessId),
        TaxYearValidation.validate(minTaxYear, data.taxYear)
      )
    }

  private def bodyFormatValidation: AmendForeignPropertyAnnualSubmissionRawData => List[List[MtdError]] = { data =>
    ???
  }

  private def bodyFieldValidation: AmendForeignPropertyAnnualSubmissionRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendForeignPropertyAnnualSubmissionRequestBody]

    List(
      flattenErrors(
        List(
          body.foreignFhlEea.map(validateForeignFhlEea).getOrElse(NoValidationErrors),
          body.foreignNonFhlProperty
            .map(_.zipWithIndex.toList.flatMap {
              case (entry, i) => validateForeignProperty(entry, i)
            })
            .getOrElse(NoValidationErrors)
        )))
  }

  private def validateForeignFhlEea(foreignFhlEea: ForeignFhlEea): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = foreignFhlEea.adjustments.flatMap(_.privateUseAdjustment),
        path = "/foreignFhlEea/adjustments/privateUseAdjustment"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.adjustments.flatMap(_.balancingCharge),
        path = "/foreignFhlEea/adjustments/balancingCharge"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.allowances.flatMap(_.annualInvestmentAllowance),
        path = "/foreignFhlEea/allowances/annualInvestmentAllowance"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.allowances.flatMap(_.otherCapitalAllowance),
        path = "/foreignFhlEea/allowances/otherCapitalAllowance"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.allowances.flatMap(_.propertyIncomeAllowance),
        path = "/foreignFhlEea/allowances/propertyAllowance"
      ),
      NumberValidation.validateOptional(
        field = foreignFhlEea.allowances.flatMap(_.electricChargePointAllowance),
        path = "/foreignFhlEea/allowances/electricChargePointAllowance"
      )
    ).flatten
  }

  private def validateForeignProperty(foreignPropertyEntry: ForeignNonFhlEntry, index: Int): List[MtdError] = {
    List(
      CountryCodeValidation.validate(
        field = foreignPropertyEntry.countryCode,
        path = s"/foreignProperty/$index/countryCode"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.adjustments.flatMap(_.privateUseAdjustment),
        path = s"/foreignProperty/$index/adjustments/privateUseAdjustment"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.adjustments.flatMap(_.balancingCharge),
        path = s"/foreignProperty/$index/adjustments/balancingCharge"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.allowances.flatMap(_.annualInvestmentAllowance),
        path = s"/foreignProperty/$index/allowances/annualInvestmentAllowance"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.allowances.flatMap(_.costOfReplacingDomesticItems),
        path = s"/foreignProperty/$index/allowances/costOfReplacingDomesticItems"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.allowances.flatMap(_.zeroEmissionsGoodsVehicleAllowance),
        path = s"/foreignProperty/$index/allowances/zeroEmissionsGoodsVehicleAllowance"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.allowances.flatMap(_.propertyIncomeAllowance),
        path = s"/foreignProperty/$index/allowances/propertyAllowance"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.allowances.flatMap(_.otherCapitalAllowance),
        path = s"/foreignProperty/$index/allowances/otherCapitalAllowance"
      ),
      NumberValidation.validateOptional(
        field = foreignPropertyEntry.allowances.flatMap(_.electricChargePointAllowance),
        path = s"/foreignProperty/$index/allowances/electricChargePointAllowance"
      )
    ).flatten
  }

  override def validate(data: AmendForeignPropertyAnnualSubmissionRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
