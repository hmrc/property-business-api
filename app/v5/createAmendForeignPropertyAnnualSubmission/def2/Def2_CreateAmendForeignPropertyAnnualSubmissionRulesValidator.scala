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

package v5.createAmendForeignPropertyAnnualSubmission.def2

import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.{ResolveIsoDate, ResolveParsedCountryCode, ResolveParsedNumber}
import shared.models.errors._
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.toTraverseOps
import common.models.errors.{RuleBothAllowancesSuppliedError, RuleBuildingNameNumberError}
import v5.createAmendForeignPropertyAnnualSubmission.def2.model.request.Def2_CreateAmendForeignPropertyAnnualSubmissionRequestData
import v5.createAmendForeignPropertyAnnualSubmission.def2.model.request.def2_foreignProperty.{
  Def2_Create_Amend_ForeignAllowances,
  Def2_Create_Amend_ForeignEntry,
  Def2_Create_Amend_StructuredBuildingAllowance
}

import java.time.LocalDate

class Def2_CreateAmendForeignPropertyAnnualSubmissionRulesValidator
    extends RulesValidator[Def2_CreateAmendForeignPropertyAnnualSubmissionRequestData] {

  private val minYear = 1900
  private val maxYear = 2100

  private val stringRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  private val resolveParsedNumber                  = ResolveParsedNumber()
  private val resolvePropertyIncomeAllowanceNumber = ResolveParsedNumber(max = 1000.00)

  private def resolveString(field: String, path: String): Validated[Seq[MtdError], Unit] =
    if (stringRegex.matches(field)) valid else Invalid(List(StringFormatError.withPath(path)))

  private def resolveStringOptional(maybeField: Option[String], path: String) =
    maybeField.map(field => resolveString(field, path)).getOrElse(valid)

  def validateBusinessRules(parsed: Def2_CreateAmendForeignPropertyAnnualSubmissionRequestData)
      : Validated[Seq[MtdError], Def2_CreateAmendForeignPropertyAnnualSubmissionRequestData] = {
    import parsed.body._
    combine(
      validateForeignEntries(foreignProperty)
    ).onSuccess(parsed)
  }

  private def validateForeignEntries(foreignEntries: Seq[Def2_Create_Amend_ForeignEntry]): Validated[Seq[MtdError], Unit] = {
    foreignEntries.zipWithIndex.toList
      .map { case (entry, index) =>
        validateForeignEntry(entry, index)
      }
      .sequence
      .andThen(_ => valid)

  }

  private def validateForeignEntry(entry: Def2_Create_Amend_ForeignEntry, index: Int): Validated[Seq[MtdError], Unit] = {
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

  private def validateForeignAllowances(index: Int)(allowances: Def2_Create_Amend_ForeignAllowances): Validated[Seq[MtdError], Unit] = {
    allowances.propertyIncomeAllowance match {
      case None => valid
      case Some(_) =>
        allowances match {
          case Def2_Create_Amend_ForeignAllowances(None, None, None, None, None, Some(_), None) => valid
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

  private def validateStructuredBuildingAllowance(structuredBuildingAllowance: Def2_Create_Amend_StructuredBuildingAllowance,
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
