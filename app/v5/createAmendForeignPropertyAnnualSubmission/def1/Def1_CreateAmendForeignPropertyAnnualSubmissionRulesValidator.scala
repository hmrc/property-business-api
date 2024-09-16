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

package v5.createAmendForeignPropertyAnnualSubmission.def1

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.{ResolveIsoDate, ResolveParsedCountryCode, ResolveParsedNumber}
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.toTraverseOps
import v5.createAmendForeignPropertyAnnualSubmission.def1.model.request.Def1_CreateAmendForeignPropertyAnnualSubmissionRequestData
import v5.createAmendForeignPropertyAnnualSubmission.def1.model.request.def1_foreignFhlEea._
import v5.createAmendForeignPropertyAnnualSubmission.def1.model.request.def1_foreignProperty._

import java.time.LocalDate

class Def1_CreateAmendForeignPropertyAnnualSubmissionRulesValidator
    extends RulesValidator[Def1_CreateAmendForeignPropertyAnnualSubmissionRequestData] {

  private val minYear = 1900
  private val maxYear = 2100

  private val stringRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  private val resolveParsedNumber                  = ResolveParsedNumber()
  private val resolvePropertyIncomeAllowanceNumber = ResolveParsedNumber(max = 1000.00)

  private def resolveString(field: String, path: String): Validated[Seq[MtdError], Unit] =
    if (stringRegex.matches(field)) valid else Invalid(List(StringFormatError.withPath(path)))

  private def resolveStringOptional(maybeField: Option[String], path: String) =
    maybeField.map(field => resolveString(field, path)).getOrElse(valid)

  def validateBusinessRules(parsed: Def1_CreateAmendForeignPropertyAnnualSubmissionRequestData)
      : Validated[Seq[MtdError], Def1_CreateAmendForeignPropertyAnnualSubmissionRequestData] = {
    import parsed.body._
    combine(
      foreignFhlEea.map(validateForeignFhlEea).getOrElse(valid),
      foreignProperty.map(validateForeignEntries).getOrElse(valid)
    ).onSuccess(parsed)
  }

  private def validateForeignFhlEea(foreignFhlEea: Def1_Create_Amend_ForeignFhlEea): Validated[Seq[MtdError], Unit] = {
    import foreignFhlEea._
    val valuesWithPaths = List(
      (adjustments.flatMap(_.privateUseAdjustment), "/foreignFhlEea/adjustments/privateUseAdjustment"),
      (adjustments.flatMap(_.balancingCharge), "/foreignFhlEea/adjustments/balancingCharge"),
      (allowances.flatMap(_.annualInvestmentAllowance), "/foreignFhlEea/allowances/annualInvestmentAllowance"),
      (allowances.flatMap(_.otherCapitalAllowance), "/foreignFhlEea/allowances/otherCapitalAllowance"),
      (allowances.flatMap(_.electricChargePointAllowance), "/foreignFhlEea/allowances/electricChargePointAllowance"),
      (allowances.flatMap(_.zeroEmissionsCarAllowance), "/foreignFhlEea/allowances/zeroEmissionsCarAllowance")
    )

    val validatedNumberFields = valuesWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    val validatedPropertyIncomeAllowance =
      resolvePropertyIncomeAllowanceNumber(allowances.flatMap(_.propertyIncomeAllowance), "/foreignFhlEea/allowances/propertyIncomeAllowance")

    val validatedAllowances = allowances.map(validateForeignFhlAllowances).getOrElse(valid)

    (validatedNumberFields :+ validatedPropertyIncomeAllowance :+ validatedAllowances).sequence.andThen(_ => valid)

  }

  def validateForeignFhlAllowances(allowances: Def1_Create_Amend_ForeignFhlEeaAllowances): Validated[Seq[MtdError], Unit] = {
    allowances.propertyIncomeAllowance match {
      case None => valid
      case Some(_) =>
        allowances match {
          case Def1_Create_Amend_ForeignFhlEeaAllowances(None, None, None, None, Some(_)) => valid
          case _ => Invalid(List(RuleBothAllowancesSuppliedError.withPath("/foreignFhlEea/allowances")))
        }
    }
  }

  private def validateForeignEntries(foreignEntries: Seq[Def1_Create_Amend_ForeignEntry]): Validated[Seq[MtdError], Unit] = {
    foreignEntries.zipWithIndex.toList
      .map { case (entry, index) =>
        validateForeignEntry(entry, index)
      }
      .sequence
      .andThen(_ => valid)

  }

  private def validateForeignEntry(entry: Def1_Create_Amend_ForeignEntry, index: Int): Validated[Seq[MtdError], Unit] = {
    import entry._
    val validatedCountryCode = ResolveParsedCountryCode(countryCode, s"/foreignProperty/$index/countryCode")

    val valuesWithPaths = List(
      (adjustments.flatMap(_.privateUseAdjustment), s"/foreignProperty/$index/adjustments/privateUseAdjustment"),
      (adjustments.flatMap(_.balancingCharge), s"/foreignProperty/$index/adjustments/balancingCharge"),
      (allowances.flatMap(_.annualInvestmentAllowance), s"/foreignProperty/$index/allowances/annualInvestmentAllowance"),
      (allowances.flatMap(_.costOfReplacingDomesticItems), s"/foreignProperty/$index/allowances/costOfReplacingDomesticItems"),
      (
        allowances.flatMap(_.zeroEmissionsGoodsVehicleAllowance),
        s"/foreignProperty/$index/allowances/zeroEmissionsGoodsVehicleAllowance"
      ),
      (allowances.flatMap(_.otherCapitalAllowance), s"/foreignProperty/$index/allowances/otherCapitalAllowance"),
      (allowances.flatMap(_.electricChargePointAllowance), s"/foreignProperty/$index/allowances/electricChargePointAllowance"),
      (allowances.flatMap(_.zeroEmissionsCarAllowance), s"/foreignProperty/$index/allowances/zeroEmissionsCarAllowance")
    )

    val validatedNumberFields = valuesWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    val validatedPropertyIncomeAllowance =
      resolvePropertyIncomeAllowanceNumber(
        allowances.flatMap(_.propertyIncomeAllowance),
        s"/foreignProperty/$index/allowances/propertyIncomeAllowance")

    val validatedBuildings = allowances
      .flatMap(_.structuredBuildingAllowance)
      .map(_.zipWithIndex.toList.map { case (structuredBuildingAllowance, i) =>
        validateStructuredBuildingAllowance(structuredBuildingAllowance, index, i)
      })
      .getOrElse(List(valid))

    val validatedAllowances = allowances.map(validateForeignAllowances(index)).getOrElse(valid)

    val validated = validatedNumberFields ++ List(validatedCountryCode, validatedPropertyIncomeAllowance, validatedAllowances) ++ validatedBuildings

    validated.sequence.andThen(_ => valid)
  }

  private def validateForeignAllowances(index: Int)(allowances: Def1_Create_Amend_ForeignAllowances): Validated[Seq[MtdError], Unit] = {
    allowances.propertyIncomeAllowance match {
      case None => valid
      case Some(_) =>
        allowances match {
          case Def1_Create_Amend_ForeignAllowances(None, None, None, None, None, None, Some(_), None) => valid
          case _ => Invalid(List(RuleBothAllowancesSuppliedError.withPath(s"/foreignProperty/$index/allowances")))
        }
    }
  }

  private def isDateWithinRange(date: Option[LocalDate], error: MtdError, path: String): Validated[Seq[MtdError], Unit] = {
    date match {
      case Some(date) => if (date.getYear >= minYear && date.getYear < maxYear) Valid(()) else Invalid(List(error.withPath(path)))
      case _          => Valid(())
    }
  }

  private def validateStructuredBuildingAllowance(structuredBuildingAllowance: Def1_Create_Amend_StructuredBuildingAllowance,
                                                  index: Int,
                                                  buildingIndex: Int): Validated[Seq[MtdError], Unit] = {
    import structuredBuildingAllowance._

    val validatedNumberAmount =
      resolveParsedNumber(amount, s"/foreignProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/amount")

    val validatedNumberFields = firstYear
      .map(_.qualifyingAmountExpenditure) match {
      case Some(number) =>
        resolveParsedNumber(
          number,
          s"/foreignProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/firstYear/qualifyingAmountExpenditure")
      case None => valid
    }

    val validatedStringFields = List(
      resolveString(building.postcode, s"/foreignProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/building/postcode"),
      resolveStringOptional(building.name, s"/foreignProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/building/name"),
      resolveStringOptional(building.number, s"/foreignProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/building/number")
    )

    val validatedDate = {
      val qualifyingDatePath = s"/foreignProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/firstYear/qualifyingDate"

      ResolveIsoDate(
        structuredBuildingAllowance.firstYear.map(_.qualifyingDate),
        DateFormatError.withPath(qualifyingDatePath)
      ).andThen(isDateWithinRange(_, DateFormatError, qualifyingDatePath))
    }

    val validatedBuilding = (building.name, building.number) match {
      case (None, None) =>
        Invalid(List(RuleBuildingNameNumberError.withPath(s"/foreignProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/building")))
      case _ => valid
    }

    val validated = List(validatedNumberAmount, validatedNumberFields, validatedDate, validatedBuilding) ++ validatedStringFields

    validated.sequence.andThen(_ => valid)
  }

}
