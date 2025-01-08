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

package v5.createAmendUkPropertyAnnualSubmission.def2

import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.{ResolveIsoDate, ResolveParsedNumber}
import shared.models.errors._
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toTraverseOps
import common.models.errors.{RuleBothAllowancesSuppliedError, RuleBuildingNameNumberError}
import v5.createAmendUkPropertyAnnualSubmission.def2.model.request.{
  Allowances,
  Def2_CreateAmendUkPropertyAnnualSubmissionRequestData,
  StructuredBuildingAllowance,
  UkProperty
}

class Def2_CreateAmendUkPropertyAnnualSubmissionRulesValidator extends RulesValidator[Def2_CreateAmendUkPropertyAnnualSubmissionRequestData] {

  private val stringRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  private val resolveParsedNumber            = ResolveParsedNumber()
  private val resolvePropertyIncomeAllowance = ResolveParsedNumber(max = 1000.00)

  private def resolveString(field: String, path: String): Validated[Seq[MtdError], Unit] =
    if (stringRegex.matches(field)) valid else Invalid(List(StringFormatError.withPath(path)))

  private def resolveStringOptional(maybeField: Option[String], path: String) =
    maybeField.map(field => resolveString(field, path)).getOrElse(valid)

  def validateBusinessRules(parsed: Def2_CreateAmendUkPropertyAnnualSubmissionRequestData)
      : Validated[Seq[MtdError], Def2_CreateAmendUkPropertyAnnualSubmissionRequestData] = {
    import parsed.body._
    combine(
      validateUkProperty(ukProperty)
    ).onSuccess(parsed)
  }

  private def validateUkProperty(ukProperty: UkProperty): Validated[Seq[MtdError], Unit] = {
    import ukProperty._

    val fieldsWithPaths = List(
      (adjustments.flatMap(_.balancingCharge), "/ukProperty/adjustments/balancingCharge"),
      (adjustments.flatMap(_.privateUseAdjustment), "/ukProperty/adjustments/privateUseAdjustment"),
      (
        adjustments.flatMap(_.businessPremisesRenovationAllowanceBalancingCharges),
        "/ukProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges"),
      (allowances.flatMap(_.annualInvestmentAllowance), "/ukProperty/allowances/annualInvestmentAllowance"),
      (allowances.flatMap(_.zeroEmissionsGoodsVehicleAllowance), "/ukProperty/allowances/zeroEmissionsGoodsVehicleAllowance"),
      (allowances.flatMap(_.businessPremisesRenovationAllowance), "/ukProperty/allowances/businessPremisesRenovationAllowance"),
      (allowances.flatMap(_.otherCapitalAllowance), "/ukProperty/allowances/otherCapitalAllowance"),
      (allowances.flatMap(_.costOfReplacingDomesticItems), "/ukProperty/allowances/costOfReplacingDomesticItems"),
      (allowances.flatMap(_.zeroEmissionsCarAllowance), "/ukProperty/allowances/zeroEmissionsCarAllowance")
    )

    val validatedPropertyIncomeAllowance =
      resolvePropertyIncomeAllowance(allowances.flatMap(_.propertyIncomeAllowance), "/ukProperty/allowances/propertyIncomeAllowance")

    val validatedNumberFields = fieldsWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      } :+ validatedPropertyIncomeAllowance

    val validatedAllowances = allowances.map(validateUkPropertyAllowances).getOrElse(valid)

    val validatedStructuredBuildingAllowance = allowances
      .flatMap(_.structuredBuildingAllowance)
      .map(_.zipWithIndex.toList.map { case (entry, index) =>
        validateBuildingAllowance(entry, index, enhanced = false)
      })
      .toList
      .flatten

    val validatedEnhancedStructuredBuildingAllowance = allowances
      .flatMap(_.enhancedStructuredBuildingAllowance)
      .map(_.zipWithIndex.toList.map { case (entry, index) =>
        validateBuildingAllowance(entry, index, enhanced = true)
      })
      .toList
      .flatten

    (validatedNumberFields ++ validatedStructuredBuildingAllowance ++ validatedEnhancedStructuredBuildingAllowance :+ validatedAllowances).sequence
      .andThen(_ => valid)
  }

  private def validateUkPropertyAllowances(allowances: Allowances): Validated[Seq[MtdError], Unit] = {
    allowances.propertyIncomeAllowance match {
      case None => valid
      case Some(_) =>
        allowances match {
          case Allowances(None, None, None, None, None, None, Some(_), None, None) => valid
          case _ => Invalid(List(RuleBothAllowancesSuppliedError.withPath("/ukProperty/allowances")))
        }
    }
  }

  private def validateBuildingAllowance(buildingAllowance: StructuredBuildingAllowance,
                                        index: Int,
                                        enhanced: Boolean): Validated[Seq[MtdError], Unit] = {
    import buildingAllowance._

    val buildingType = if (enhanced) "enhancedStructuredBuildingAllowance" else "structuredBuildingAllowance"

    val validatedNumberFields = List(
      (firstYear.map(_.qualifyingAmountExpenditure), s"/ukProperty/allowances/$buildingType/$index/firstYear/qualifyingAmountExpenditure")
    ).map {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, path)
    } :+ resolveParsedNumber(amount, s"/ukProperty/allowances/$buildingType/$index/amount")

    val validatedDateField = ResolveIsoDate(
      firstYear.map(_.qualifyingDate),
      DateFormatError.withPath(s"/ukProperty/allowances/$buildingType/$index/firstYear/qualifyingDate"))

    val validatedBuildingField = (building.name, building.number) match {
      case (None, None) =>
        Invalid(List(RuleBuildingNameNumberError.withPath(s"/ukProperty/allowances/$buildingType/$index/building")))
      case _ => valid
    }

    val validatedStringFields = List(
      resolveStringOptional(building.name, s"/ukProperty/allowances/$buildingType/$index/building/name"),
      resolveStringOptional(building.number, s"/ukProperty/allowances/$buildingType/$index/building/number"),
      resolveString(building.postcode, s"/ukProperty/allowances/$buildingType/$index/building/postcode")
    )

    (validatedNumberFields ++ validatedStringFields :+ validatedDateField :+ validatedBuildingField).sequence.andThen(_ => valid)
  }

}
