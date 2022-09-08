/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.{ JsValue, Json, Reads }
import v2.controllers.requestParsers.validators.validations.JsonFormatValidation.validateAndCheckNonEmptyOrRead
import v2.controllers.requestParsers.validators.validations.NumberValidation.{ validateOptional => optionalNumber }
import v2.controllers.requestParsers.validators.validations.{ NinoValidation, TaxYearValidation }
import v2.models.errors.MtdError
import v2.models.request.RawData
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom

import javax.inject.Singleton

// FIXME replace with real ones
case class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData(nino: String, taxYear: String, body: JsValue) extends RawData

case class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody(annualAdjustments: Option[HistoricNonFhlAnnualAdjustments],
                                                                          annualAllowances: Option[HistoricNonFhlAnnualAllowances])

object CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody {
  implicit val reads: Reads[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody] = Json.reads
}

case class HistoricNonFhlAnnualAdjustments(lossBroughtForward: Option[BigDecimal],
                                           privateUseAdjustment: Option[BigDecimal],
                                           balancingCharge: Option[BigDecimal],
                                           businessPremisesRenovationAllowanceBalancingCharges: Option[BigDecimal],
                                           nonResidentLandlord: Boolean,
                                           rentARoom: Option[UkPropertyAdjustmentsRentARoom])

object HistoricNonFhlAnnualAdjustments {
  implicit val reads: Reads[HistoricNonFhlAnnualAdjustments] = Json.reads
}
case class HistoricNonFhlAnnualAllowances(annualInvestmentAllowance: Option[BigDecimal],
                                          businessPremisesRenovationAllowance: Option[BigDecimal],
                                          otherCapitalAllowance: Option[BigDecimal],
                                          propertyIncomeAllowance: Option[BigDecimal],
                                          zeroEmissionGoodsVehicleAllowance: Option[BigDecimal],
                                          costOfReplacingDomesticGoods: Option[BigDecimal])

object HistoricNonFhlAnnualAllowances {
  implicit val reads: Reads[HistoricNonFhlAnnualAllowances] = Json.reads
}

@Singleton
class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator @Inject()(appConfig: AppConfig)
    extends Validator[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData] {

  lazy private val minTaxYear = appConfig.minimumTaxHistoric

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
        TaxYearValidation.validate(minTaxYear, data.taxYear)

    errorsResult(errors)
  }

  private def validateBusinessRules(body: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody): Either[List[MtdError], Unit] = {
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
          optionalNumber(zeroEmissionGoodsVehicleAllowance, "/annualAllowances/zeroEmissionGoodsVehicleAllowance") ++
          optionalNumber(businessPremisesRenovationAllowance, "/annualAllowances/businessPremisesRenovationAllowance") ++
          optionalNumber(otherCapitalAllowance, "/annualAllowances/otherCapitalAllowance") ++
          optionalNumber(costOfReplacingDomesticGoods, "/annualAllowances/costOfReplacingDomesticGoods") ++
          optionalNumber(propertyIncomeAllowance, "/annualAllowances/propertyIncomeAllowance")
      }
      .getOrElse(Nil)

    errorsResult(annualAdjustmentErrors ++ annualAllowanceErrors)
  }
}
