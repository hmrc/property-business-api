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

package v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveHistoricTaxYear, ResolveNino, ResolveNonEmptyJsonObject, ResolveParsedNumber}
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits.{catsSyntaxTuple3Semigroupal, toTraverseOps}
import config.AppConfig
import play.api.libs.json.JsValue
import v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.def1.model.request.{HistoricNonFhlAnnualAdjustments, HistoricNonFhlAnnualAllowances}
import v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.model.request.{CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData, Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody, Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData}

class Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator(nino: String, taxYear: String, body: JsValue)(implicit appConfig: AppConfig)
    extends Validator[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] {

  private lazy val minimumTaxYear = appConfig.minimumTaxYearHistoric
  private lazy val maximumTaxYear = appConfig.maximumTaxYearHistoric

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody]()

  private val resolveParsedNumber                        = ResolveParsedNumber()
  private val resolveParsedNumberPropertyIncomeAllowance = ResolveParsedNumber(max = 1000)

  private val valid = Valid(())

  def validate: Validated[Seq[MtdError], Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      ResolveHistoricTaxYear(minimumTaxYear, maximumTaxYear, taxYear),
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
