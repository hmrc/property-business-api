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

package v2.controllers.validators

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveHistoricTaxYear, ResolveNino, ResolveNonEmptyJsonObject, ResolveParsedNumber}
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits.{catsSyntaxTuple3Semigroupal, toTraverseOps}
import api.config.AppConfig
import play.api.libs.json.JsValue
import v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission.{
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody,
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData,
  HistoricNonFhlAnnualAdjustments,
  HistoricNonFhlAnnualAllowances
}

import javax.inject.{Inject, Singleton}

@Singleton
class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory @Inject() (appConfig: AppConfig) {

  private lazy val minimumTaxYear = appConfig.minimumTaxHistoric + 1
  private lazy val maximumTaxYear = appConfig.maximumTaxHistoric

  private val resolveJson = new ResolveNonEmptyJsonObject[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody]()

  private val resolveParsedNumber                        = ResolveParsedNumber()
  private val resolveParsedNumberPropertyIncomeAllowance = ResolveParsedNumber(max = 1000)

  private val valid = Valid(())

  def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
    new Validator[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] {

      def validate: Validated[Seq[MtdError], CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
        (
          ResolveNino(nino),
          ResolveHistoricTaxYear(minimumTaxYear, maximumTaxYear, taxYear, None, None),
          resolveJson(body)
        ).mapN(CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData) andThen validateBusinessRules

      private def validateBusinessRules(parsed: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData)
          : Validated[Seq[MtdError], CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] = {
        import parsed.body._

        val validatedAnnualAdjustments = annualAdjustments.map(validateAnnualAdjustments).getOrElse(valid)
        val validatedAnnualAllowances  = annualAllowances.map(validateAnnualAllowances).getOrElse(valid)

        List(
          validatedAnnualAdjustments,
          validatedAnnualAllowances
        )
          .traverse(identity)
          .map(_ => parsed)
      }

    }

  def validateAnnualAdjustments(annualAdjustments: HistoricNonFhlAnnualAdjustments): Validated[Seq[MtdError], Unit] = {
    import annualAdjustments._

    val valuesWithPaths = List(
      (lossBroughtForward, "/annualAdjustments/lossBroughtForward"),
      (privateUseAdjustment, "/annualAdjustments/privateUseAdjustment"),
      (balancingCharge, "/annualAdjustments/balancingCharge"),
      (businessPremisesRenovationAllowanceBalancingCharges, "/annualAdjustments/businessPremisesRenovationAllowanceBalancingCharges")
    )

    val validatedNumberFields = valuesWithPaths.map {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, None, Some(path))
    }

    validatedNumberFields.sequence.andThen(_ => valid)
  }

  def validateAnnualAllowances(annualAllowances: HistoricNonFhlAnnualAllowances): Validated[Seq[MtdError], Unit] = {
    import annualAllowances._

    val valuesWithPaths = List(
      (annualInvestmentAllowance, "/annualAllowances/annualInvestmentAllowance"),
      (zeroEmissionGoodsVehicleAllowance, "/annualAllowances/zeroEmissionGoodsVehicleAllowance"),
      (businessPremisesRenovationAllowance, "/annualAllowances/businessPremisesRenovationAllowance"),
      (otherCapitalAllowance, "/annualAllowances/otherCapitalAllowance"),
      (costOfReplacingDomesticGoods, "/annualAllowances/costOfReplacingDomesticGoods")
    )

    val validatedPropertyIncomeAllowance = propertyIncomeAllowance
      .map(number => resolveParsedNumberPropertyIncomeAllowance(number, None, Some("/annualAllowances/propertyIncomeAllowance")))
      .getOrElse(valid)

    val validatedNumberFields = valuesWithPaths.map {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, None, Some(path))
    }

    (validatedNumberFields :+ validatedPropertyIncomeAllowance).sequence.andThen(_ => valid)
  }

}
