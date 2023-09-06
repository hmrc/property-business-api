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
import cats.implicits._
import config.AppConfig
import play.api.libs.json.JsValue
import v2.models.request.createAmendHistoricFhlUkPropertyAnnualSubmission._

import javax.inject.{Inject, Singleton}
import scala.annotation.nowarn

@Singleton
class CreateAmendHistoricFhlUkPropertyAnnualSubmissionValidatorFactory @Inject() (appConfig: AppConfig) {

  @nowarn("cat=lint-byname-implicit")
  private val resolveJson = new ResolveNonEmptyJsonObject[CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody]()

  private val resolveParsedNumber = ResolveParsedNumber()

  private val valid = Valid(())

  def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
    new Validator[CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] {

      def validate: Validated[Seq[MtdError], CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
        (
          ResolveNino(nino),
          ResolveHistoricTaxYear(appConfig.minimumTaxHistoric + 1, appConfig.maximumTaxHistoric, taxYear, None, None),
          resolveJson(body)
        ).mapN(CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData) andThen validateBusinessRules

      private def validateBusinessRules(parsed: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData)
          : Validated[Seq[MtdError], CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] = {
        import parsed.body._

        List(
          annualAdjustments.map(validateAnnualAdjustments).getOrElse(valid),
          annualAllowances.map(validateAnnualAllowance).getOrElse(valid)
        ).traverse(identity)
          .map(_ => parsed)
      }

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
        case (Some(number), path) => resolveParsedNumber(number, None, Some(path))
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
      resolvePropertyIncomeAllowanceNumber(propertyIncomeAllowance, None, Some("/annualAllowances/propertyIncomeAllowance"))

    val result = annualAllowancesWithPaths.map {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, None, Some(path))
    } :+ propertyIncomeAllowanceResult

    result.sequence.andThen(_ => valid)
  }

}
