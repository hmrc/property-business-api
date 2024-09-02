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

import api.controllers.validators.RulesValidator
import api.controllers.validators.resolvers.{ResolveIsoDate, ResolveParsedCountryCode, ResolveParsedNumber}
import api.models.errors.{DateFormatError, MtdError, RuleBothAllowancesSuppliedError, RuleBuildingNameNumberError, StringFormatError}
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.toTraverseOps
import v2.models.request.common.StructuredBuildingAllowance
import v2.models.request.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionRequestData
import v2.models.request.createAmendForeignPropertyAnnualSubmission.foreignFhlEea.{ForeignFhlEea, ForeignFhlEeaAllowances}
import v2.models.request.createAmendForeignPropertyAnnualSubmission.foreignNonFhl.{ForeignNonFhlAllowances, ForeignNonFhlEntry}

import java.time.LocalDate

object CreateAmendForeignPropertyAnnualSubmissionValidator extends RulesValidator[CreateAmendForeignPropertyAnnualSubmissionRequestData] {

  private val minYear = 1900
  private val maxYear = 2100

  private val stringRegex = "^[0-9a-zA-Z{À-˿’}\\- _&`():.'^]{1,90}$".r

  private val resolveParsedNumber                  = ResolveParsedNumber()
  private val resolvePropertyIncomeAllowanceNumber = ResolveParsedNumber(max = 1000.00)

  private def resolveString(field: String, path: String): Validated[Seq[MtdError], Unit] =
    if (stringRegex.matches(field)) valid else Invalid(List(StringFormatError.withPath(path)))

  private def resolveStringOptional(maybeField: Option[String], path: String) =
    maybeField.map(field => resolveString(field, path)).getOrElse(valid)

  def validateBusinessRules(parsed: CreateAmendForeignPropertyAnnualSubmissionRequestData)
      : Validated[Seq[MtdError], CreateAmendForeignPropertyAnnualSubmissionRequestData] = {
    import parsed.body._

    List(
      foreignFhlEea.map(validateForeignFhlEea).getOrElse(valid),
      foreignNonFhlProperty.map(validateForeignNonFhlEntries).getOrElse(valid)
    ).traverse(identity)
      .map(_ => parsed)
  }

  private def validateForeignFhlEea(foreignFhlEea: ForeignFhlEea): Validated[Seq[MtdError], Unit] = {
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

  def validateForeignFhlAllowances(allowances: ForeignFhlEeaAllowances): Validated[Seq[MtdError], Unit] = {
    allowances.propertyIncomeAllowance match {
      case None => valid
      case Some(_) =>
        allowances match {
          case ForeignFhlEeaAllowances(None, None, None, None, Some(_)) => valid
          case _ => Invalid(List(RuleBothAllowancesSuppliedError.withPath("/foreignFhlEea/allowances")))
        }
    }
  }

  private def validateForeignNonFhlEntries(foreignNonFhlEntries: Seq[ForeignNonFhlEntry]): Validated[Seq[MtdError], Unit] = {
    foreignNonFhlEntries.zipWithIndex.toList
      .map { case (entry, index) =>
        validateForeignNonFhlEntry(entry, index)
      }
      .sequence
      .andThen(_ => valid)

  }

  private def validateForeignNonFhlEntry(entry: ForeignNonFhlEntry, index: Int): Validated[Seq[MtdError], Unit] = {
    import entry._

    val validatedCountryCode = ResolveParsedCountryCode(countryCode, s"/foreignNonFhlProperty/$index/countryCode")

    val valuesWithPaths = List(
      (adjustments.flatMap(_.privateUseAdjustment), s"/foreignNonFhlProperty/$index/adjustments/privateUseAdjustment"),
      (adjustments.flatMap(_.balancingCharge), s"/foreignNonFhlProperty/$index/adjustments/balancingCharge"),
      (allowances.flatMap(_.annualInvestmentAllowance), s"/foreignNonFhlProperty/$index/allowances/annualInvestmentAllowance"),
      (allowances.flatMap(_.costOfReplacingDomesticItems), s"/foreignNonFhlProperty/$index/allowances/costOfReplacingDomesticItems"),
      (
        allowances.flatMap(_.zeroEmissionsGoodsVehicleAllowance),
        s"/foreignNonFhlProperty/$index/allowances/zeroEmissionsGoodsVehicleAllowance"
      ),
      (allowances.flatMap(_.otherCapitalAllowance), s"/foreignNonFhlProperty/$index/allowances/otherCapitalAllowance"),
      (allowances.flatMap(_.electricChargePointAllowance), s"/foreignNonFhlProperty/$index/allowances/electricChargePointAllowance"),
      (allowances.flatMap(_.zeroEmissionsCarAllowance), s"/foreignNonFhlProperty/$index/allowances/zeroEmissionsCarAllowance")
    )

    val validatedNumberFields = valuesWithPaths
      .map {
        case (None, _)            => valid
        case (Some(number), path) => resolveParsedNumber(number, path)
      }

    val validatedPropertyIncomeAllowance =
      resolvePropertyIncomeAllowanceNumber(
        allowances.flatMap(_.propertyIncomeAllowance),
        s"/foreignNonFhlProperty/$index/allowances/propertyIncomeAllowance")

    val validatedBuildings = allowances
      .flatMap(_.structuredBuildingAllowance)
      .map(_.zipWithIndex.toList.map { case (structuredBuildingAllowance, i) =>
        validateStructuredBuildingAllowance(structuredBuildingAllowance, index, i)
      })
      .getOrElse(List(valid))

    val validatedAllowances = allowances.map(validateForeignNonFhlAllowances(index)).getOrElse(valid)

    val validated = validatedNumberFields ++ List(validatedCountryCode, validatedPropertyIncomeAllowance, validatedAllowances) ++ validatedBuildings

    validated.sequence.andThen(_ => valid)
  }

  private def validateForeignNonFhlAllowances(index: Int)(allowances: ForeignNonFhlAllowances): Validated[Seq[MtdError], Unit] = {
    allowances.propertyIncomeAllowance match {
      case None => valid
      case Some(_) =>
        allowances match {
          case ForeignNonFhlAllowances(None, None, None, None, None, None, Some(_), None) => valid
          case _ => Invalid(List(RuleBothAllowancesSuppliedError.withPath(s"/foreignNonFhlProperty/$index/allowances")))
        }
    }
  }

  private def isDateWithinRange(date: Option[LocalDate], error: MtdError, path: String): Validated[Seq[MtdError], Unit] = {
    date match {
      case Some(date) => if (date.getYear >= minYear && date.getYear < maxYear) Valid(()) else Invalid(List(error.withPath(path)))
      case _          => Valid(())
    }
  }

  private def validateStructuredBuildingAllowance(structuredBuildingAllowance: StructuredBuildingAllowance,
                                                  index: Int,
                                                  buildingIndex: Int): Validated[Seq[MtdError], Unit] = {
    import structuredBuildingAllowance._

    val validatedNumberAmount =
      resolveParsedNumber(amount, s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/amount")

    val validatedNumberFields = firstYear
      .map(_.qualifyingAmountExpenditure) match {
      case Some(number) =>
        resolveParsedNumber(
          number,
          s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/firstYear/qualifyingAmountExpenditure")
      case None => valid
    }

    val validatedStringFields = List(
      resolveString(building.postcode, s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/building/postcode"),
      resolveStringOptional(building.name, s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/building/name"),
      resolveStringOptional(building.number, s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/building/number")
    )

    val validatedDate = {
      val qualifyingDatePath = s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/firstYear/qualifyingDate"

      ResolveIsoDate(
        structuredBuildingAllowance.firstYear.map(_.qualifyingDate),
        DateFormatError.withPath(qualifyingDatePath)
      ).andThen(isDateWithinRange(_, DateFormatError, qualifyingDatePath))
    }

    val validatedBuilding = (building.name, building.number) match {
      case (None, None) =>
        Invalid(
          List(RuleBuildingNameNumberError.withPath(s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$buildingIndex/building")))
      case _ => valid
    }

    val validated = List(validatedNumberAmount, validatedNumberFields, validatedDate, validatedBuilding) ++ validatedStringFields

    validated.sequence.andThen(_ => valid)
  }

}
