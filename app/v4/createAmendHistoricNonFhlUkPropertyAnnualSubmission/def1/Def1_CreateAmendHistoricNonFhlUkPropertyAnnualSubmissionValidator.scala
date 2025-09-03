/*
 * Copyright 2025 HM Revenue & Customs
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

package v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.def1

import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits.{catsSyntaxTuple3Semigroupal, toTraverseOps}
import common.models.errors.RuleHistoricTaxYearNotSupportedError
import config.PropertyBusinessConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveParsedNumber, ResolveTaxYearMinMax}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.def1.model.request.{HistoricNonFhlAnnualAdjustments, HistoricNonFhlAnnualAllowances}
import v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.model.request.*

import javax.inject.Inject

class Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator @Inject() (nino: String, taxYear: String, body: JsValue)(implicit
    config: PropertyBusinessConfig)
    extends Validator[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] {

  private val resolveTaxYear =
    ResolveTaxYearMinMax(
      (TaxYear.fromMtd(config.historicMinimumTaxYear), TaxYear.fromMtd(config.historicMaximumTaxYear)),
      RuleHistoricTaxYearNotSupportedError)

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody]()

  private val resolveParsedNumber                        = ResolveParsedNumber()
  private val resolveParsedNumberPropertyIncomeAllowance = ResolveParsedNumber(max = 1000)

  private val valid = Valid(())

  def validate: Validated[Seq[MtdError], Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData) andThen validateBusinessRules

  private def validateBusinessRules(parsed: Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData)
      : Validated[Seq[MtdError], Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] = {

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
      case (Some(number), path) => resolveParsedNumber(number, path)
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
      .map(number => resolveParsedNumberPropertyIncomeAllowance(number, path = "/annualAllowances/propertyIncomeAllowance"))
      .getOrElse(valid)

    val validatedNumberFields = valuesWithPaths.map {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, path)
    }

    (validatedNumberFields :+ validatedPropertyIncomeAllowance).sequence.andThen(_ => valid)
  }

}
