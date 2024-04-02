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

package v4.controllers.createAmendUkPropertyAnnualSubmission.def1

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.{ResolveIsoDate, ResolveParsedNumber}
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits.toTraverseOps
import v4.controllers.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukFhlProperty._
import v4.controllers.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukNonFhlProperty._
import v4.controllers.createAmendUkPropertyAnnualSubmission.model.request.Def1_CreateAmendUkPropertyAnnualSubmissionRequestData

class Def1_CreateAmendUkPropertyAnnualSubmissionRulesValidator extends RulesValidator[Def1_CreateAmendUkPropertyAnnualSubmissionRequestData] {

  private val stringRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  private val resolveParsedNumber            = ResolveParsedNumber()
  private val resolvePropertyIncomeAllowance = ResolveParsedNumber(max = 1000.00)

  private def resolveString(field: String, path: String): Validated[Seq[MtdError], Unit] =
    if (stringRegex.matches(field)) valid else Invalid(List(StringFormatError.withPath(path)))

  private def resolveStringOptional(maybeField: Option[String], path: String) =
    maybeField.map(field => resolveString(field, path)).getOrElse(valid)

  def validateBusinessRules(parsed: Def1_CreateAmendUkPropertyAnnualSubmissionRequestData)
      : Validated[Seq[MtdError], Def1_CreateAmendUkPropertyAnnualSubmissionRequestData] = {
    import parsed.body._
    combine(
      ukFhlProperty.map(validateUkFhlProperty).getOrElse(valid),
      ukNonFhlProperty.map(validateUkNonFhlProperty).getOrElse(valid)
    ).onSuccess(parsed)
  }

  private def validateUkFhlProperty(ukFhlProperty: Def1_Create_Amend_UkFhlProperty): Validated[Seq[MtdError], Unit] = {
    import ukFhlProperty._

    val fieldsWithPaths = List(
      (adjustments.flatMap(_.balancingCharge), "/ukFhlProperty/adjustments/balancingCharge"),
      (adjustments.flatMap(_.privateUseAdjustment), "/ukFhlProperty/adjustments/privateUseAdjustment"),
      (
        adjustments.flatMap(_.businessPremisesRenovationAllowanceBalancingCharges),
        "/ukFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges"),
      (allowances.flatMap(_.annualInvestmentAllowance), "/ukFhlProperty/allowances/annualInvestmentAllowance"),
      (allowances.flatMap(_.businessPremisesRenovationAllowance), "/ukFhlProperty/allowances/businessPremisesRenovationAllowance"),
      (allowances.flatMap(_.otherCapitalAllowance), "/ukFhlProperty/allowances/otherCapitalAllowance"),
      (allowances.flatMap(_.electricChargePointAllowance), "/ukFhlProperty/allowances/electricChargePointAllowance"),
      (allowances.flatMap(_.zeroEmissionsCarAllowance), "/ukFhlProperty/allowances/zeroEmissionsCarAllowance")
    )

    val validatedPropertyIncomeAllowance =
      resolvePropertyIncomeAllowance(allowances.flatMap(_.propertyIncomeAllowance), "/ukFhlProperty/allowances/propertyIncomeAllowance")

    val validatedNumberFields = fieldsWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      } :+ validatedPropertyIncomeAllowance

    val validatedAllowances = allowances.map(validateUkFhlAllowances).getOrElse(valid)

    (validatedNumberFields :+ validatedAllowances).sequence.andThen(_ => valid)
  }

  private def validateUkFhlAllowances(allowances: Def1_Create_Amend_UkFhlPropertyAllowances): Validated[Seq[MtdError], Unit] = {
    allowances.propertyIncomeAllowance match {
      case None => valid
      case Some(_) =>
        allowances match {
          case Def1_Create_Amend_UkFhlPropertyAllowances(None, None, None, None, None, Some(_)) => valid
          case _ => Invalid(List(RuleBothAllowancesSuppliedError.withPath("/ukFhlProperty/allowances")))
        }
    }
  }

  private def validateUkNonFhlProperty(ukNonFhlProperty: Def1_Create_Amend_UkNonFhlProperty): Validated[Seq[MtdError], Unit] = {
    import ukNonFhlProperty._

    val fieldsWithPaths = List(
      (adjustments.flatMap(_.balancingCharge), "/ukNonFhlProperty/adjustments/balancingCharge"),
      (adjustments.flatMap(_.privateUseAdjustment), "/ukNonFhlProperty/adjustments/privateUseAdjustment"),
      (
        adjustments.flatMap(_.businessPremisesRenovationAllowanceBalancingCharges),
        "/ukNonFhlProperty/adjustments/businessPremisesRenovationAllowanceBalancingCharges"),
      (allowances.flatMap(_.annualInvestmentAllowance), "/ukNonFhlProperty/allowances/annualInvestmentAllowance"),
      (allowances.flatMap(_.zeroEmissionsGoodsVehicleAllowance), "/ukNonFhlProperty/allowances/zeroEmissionsGoodsVehicleAllowance"),
      (allowances.flatMap(_.businessPremisesRenovationAllowance), "/ukNonFhlProperty/allowances/businessPremisesRenovationAllowance"),
      (allowances.flatMap(_.otherCapitalAllowance), "/ukNonFhlProperty/allowances/otherCapitalAllowance"),
      (allowances.flatMap(_.costOfReplacingDomesticGoods), "/ukNonFhlProperty/allowances/costOfReplacingDomesticGoods"),
      (allowances.flatMap(_.electricChargePointAllowance), "/ukNonFhlProperty/allowances/electricChargePointAllowance"),
      (allowances.flatMap(_.zeroEmissionsCarAllowance), "/ukNonFhlProperty/allowances/zeroEmissionsCarAllowance")
    )

    val validatedPropertyIncomeAllowance =
      resolvePropertyIncomeAllowance(allowances.flatMap(_.propertyIncomeAllowance), "/ukNonFhlProperty/allowances/propertyIncomeAllowance")

    val validatedNumberFields = fieldsWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      } :+ validatedPropertyIncomeAllowance

    val validatedAllowances = allowances.map(validateUkNonFhlAllowances).getOrElse(valid)

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

  private def validateUkNonFhlAllowances(allowances: Def1_Create_Amend_UkNonFhlPropertyAllowances): Validated[Seq[MtdError], Unit] = {
    allowances.propertyIncomeAllowance match {
      case None => valid
      case Some(_) =>
        allowances match {
          case Def1_Create_Amend_UkNonFhlPropertyAllowances(None, None, None, None, None, None, None, Some(_), None, None) => valid
          case _ => Invalid(List(RuleBothAllowancesSuppliedError.withPath("/ukNonFhlProperty/allowances")))
        }
    }
  }

  private def validateBuildingAllowance(buildingAllowance: Def1_Create_Amend_StructuredBuildingAllowance,
                                        index: Int,
                                        enhanced: Boolean): Validated[Seq[MtdError], Unit] = {
    import buildingAllowance._

    val buildingType = if (enhanced) "enhancedStructuredBuildingAllowance" else "structuredBuildingAllowance"

    val validatedNumberFields = List(
      (firstYear.map(_.qualifyingAmountExpenditure), s"/ukNonFhlProperty/allowances/$buildingType/$index/firstYear/qualifyingAmountExpenditure")
    ).map {
      case (None, _)            => valid
      case (Some(number), path) => resolveParsedNumber(number, path)
    } :+ resolveParsedNumber(amount, s"/ukNonFhlProperty/allowances/$buildingType/$index/amount")

    val validatedDateField = ResolveIsoDate(
      firstYear.map(_.qualifyingDate),
      DateFormatError.withPath(s"/ukNonFhlProperty/allowances/$buildingType/$index/firstYear/qualifyingDate"))

    val validatedBuildingField = (building.name, building.number) match {
      case (None, None) =>
        Invalid(List(RuleBuildingNameNumberError.withPath(s"/ukNonFhlProperty/allowances/$buildingType/$index/building")))
      case _ => valid
    }

    val validatedStringFields = List(
      resolveStringOptional(building.name, s"/ukNonFhlProperty/allowances/$buildingType/$index/building/name"),
      resolveStringOptional(building.number, s"/ukNonFhlProperty/allowances/$buildingType/$index/building/number"),
      resolveString(building.postcode, s"/ukNonFhlProperty/allowances/$buildingType/$index/building/postcode")
    )

    (validatedNumberFields ++ validatedStringFields :+ validatedDateField :+ validatedBuildingField).sequence.andThen(_ => valid)
  }

}
