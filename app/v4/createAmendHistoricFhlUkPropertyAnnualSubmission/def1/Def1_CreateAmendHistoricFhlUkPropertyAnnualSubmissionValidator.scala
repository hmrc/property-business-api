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

package v4.createAmendHistoricFhlUkPropertyAnnualSubmission.def1

import cats.data.Validated
import cats.data.Validated._
import cats.implicits._
import common.models.errors.RuleHistoricTaxYearNotSupportedError
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject, ResolveParsedNumber, ResolveTaxYearMinMax}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v4.createAmendHistoricFhlUkPropertyAnnualSubmission.def1.model.request._
import v4.createAmendHistoricFhlUkPropertyAnnualSubmission.model.request._

class Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionValidator(nino: String, taxYear: String, body: JsValue)
    extends Validator[CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] {

  private val resolveTaxYear = ResolveTaxYearMinMax((TaxYear.fromMtd("2017-18"), TaxYear.fromMtd("2021-22")), RuleHistoricTaxYearNotSupportedError)

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody]()

  private val resolveParsedNumber = ResolveParsedNumber()

  private val valid = Valid(())

  def validate: Validated[Seq[MtdError], CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData) andThen validateBusinessRules

  private def validateBusinessRules(parsed: Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData)
      : Validated[Seq[MtdError], Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] = {
    import parsed.body._

    List(
      annualAdjustments.map(validateAnnualAdjustments).getOrElse(valid),
      annualAllowances.map(validateAnnualAllowance).getOrElse(valid)
    ).traverse(identity)
      .map(_ => parsed)
  }

  private def validateAnnualAdjustments(annualAdjustments: HistoricFhlAnnualAdjustments): Validated[Seq[MtdError], Unit] = {
    import annualAdjustments._
    val annualAdjustmentsWithPaths = List(
      (lossBroughtForward, "/annualAdjustments/lossBroughtForward"),
      (privateUseAdjustment, "/annualAdjustments/privateUseAdjustment"),
      (balancingCharge, "/annualAdjustments/balancingCharge"),
      (businessPremisesRenovationAllowanceBalancingCharges, "/annualAdjustments/businessPremisesRenovationAllowanceBalancingCharges")
    )

    val result = annualAdjustmentsWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    result.sequence.andThen(_ => valid)
  }

  private def validateAnnualAllowance(annualAllowances: HistoricFhlAnnualAllowances): Validated[Seq[MtdError], Unit] = {
    import annualAllowances._
    val resolvePropertyIncomeAllowanceNumber = ResolveParsedNumber(max = 1000.00)

    val annualAllowancesWithPaths = List(
      (annualInvestmentAllowance, "/annualAllowances/annualInvestmentAllowance"),
      (otherCapitalAllowance, "/annualAllowances/otherCapitalAllowance"),
      (businessPremisesRenovationAllowance, "/annualAllowances/businessPremisesRenovationAllowance")
    )

    val propertyIncomeAllowanceResult =
      resolvePropertyIncomeAllowanceNumber(propertyIncomeAllowance, "/annualAllowances/propertyIncomeAllowance")

    val result = annualAllowancesWithPaths.map {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, path)
    } :+ propertyIncomeAllowanceResult

    result.sequence.andThen(_ => valid)
  }

}
