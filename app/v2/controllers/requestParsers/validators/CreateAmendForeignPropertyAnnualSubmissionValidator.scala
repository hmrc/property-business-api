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

package v2.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations.{BusinessIdValidation, CountryCodeValidation, DateValidation, JsonFormatValidation, NinoValidation, NoValidationErrors, NumberValidation, StringValidation, TaxYearValidation}
import config.AppConfig
import v2.controllers.requestParsers.validators.validations._
import api.models.errors._
import v2.models.request.createAmendForeignPropertyAnnualSubmission.foreignFhlEea.ForeignFhlEea
import v2.models.request.createAmendForeignPropertyAnnualSubmission.foreignNonFhl.ForeignNonFhlEntry
import v2.models.request.createAmendForeignPropertyAnnualSubmission.{CreateAmendForeignPropertyAnnualSubmissionRawData, CreateAmendForeignPropertyAnnualSubmissionRequestBody}
import v2.models.request.common.StructuredBuildingAllowance

import javax.inject.{Inject, Singleton}

@Singleton
class CreateAmendForeignPropertyAnnualSubmissionValidator @Inject()(appConfig: AppConfig)
    extends Validator[CreateAmendForeignPropertyAnnualSubmissionRawData] {

  private lazy val minTaxYear = appConfig.minimumTaxV2Foreign
  private val validationSet   = List(parameterFormatValidation, bodyFormatValidation, bodyFieldValidation)

  private def parameterFormatValidation: CreateAmendForeignPropertyAnnualSubmissionRawData => List[List[MtdError]] =
    (data: CreateAmendForeignPropertyAnnualSubmissionRawData) => {
      List(
        NinoValidation.validate(data.nino),
        BusinessIdValidation.validate(data.businessId),
        TaxYearValidation.validate(minTaxYear, data.taxYear)
      )
    }

  private def bodyFormatValidation: CreateAmendForeignPropertyAnnualSubmissionRawData => List[List[MtdError]] = { data =>
    JsonFormatValidation.validateAndCheckNonEmpty[CreateAmendForeignPropertyAnnualSubmissionRequestBody](data.body) match {
      case Nil          => NoValidationErrors
      case schemaErrors => List(schemaErrors)
    }
  }

  private def bodyFieldValidation: CreateAmendForeignPropertyAnnualSubmissionRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[CreateAmendForeignPropertyAnnualSubmissionRequestBody]

    List(
      flattenErrors(
        List(
          body.foreignFhlEea.map(validateForeignFhlEea).getOrElse(NoValidationErrors),
          body.foreignNonFhlProperty
            .map(_.zipWithIndex.toList.flatMap {
              case (entry, i) => validateForeignProperty(entry, i)
            })
            .getOrElse(NoValidationErrors),
          duplicateCountryCodeValidation(body)
        )))
  }

  private def validateForeignFhlEea(foreignFhlEea: ForeignFhlEea): List[MtdError] = {
    List(
      NumberValidation.validateOptional(field = foreignFhlEea.adjustments.flatMap(_.privateUseAdjustment),
                                        path = "/foreignFhlEea/adjustments/privateUseAdjustment"),
      NumberValidation.validateOptional(field = foreignFhlEea.adjustments.flatMap(_.balancingCharge),
                                        path = "/foreignFhlEea/adjustments/balancingCharge"),
      NumberValidation.validateOptional(field = foreignFhlEea.allowances.flatMap(_.annualInvestmentAllowance),
                                        path = "/foreignFhlEea/allowances/annualInvestmentAllowance"),
      NumberValidation.validateOptional(field = foreignFhlEea.allowances.flatMap(_.otherCapitalAllowance),
                                        path = "/foreignFhlEea/allowances/otherCapitalAllowance"),
      NumberValidation.validateOptional(field = foreignFhlEea.allowances.flatMap(_.electricChargePointAllowance),
                                        path = "/foreignFhlEea/allowances/electricChargePointAllowance"),
      NumberValidation.validateOptional(field = foreignFhlEea.allowances.flatMap(_.zeroEmissionsCarAllowance),
                                        path = "/foreignFhlEea/allowances/zeroEmissionsCarAllowance"),
      NumberValidation.validateOptional(field = foreignFhlEea.allowances.flatMap(_.propertyIncomeAllowance),
                                        path = "/foreignFhlEea/allowances/propertyIncomeAllowance",
                                        max = 1000),
      foreignFhlEea.allowances
        .map(allowances => AllowancesValidation.validateForeignFhl(allowances = allowances, path = "/foreignFhlEea/allowances"))
        .getOrElse(Nil),
      validateFhlPropertyIncomeAllowance(foreignFhlEea)
    ).flatten
  }

  private def validateForeignProperty(entry: ForeignNonFhlEntry, index: Int): List[MtdError] = {

    List(
      CountryCodeValidation.validate(field = entry.countryCode, path = s"/foreignNonFhlProperty/$index/countryCode"),
      NumberValidation.validateOptional(field = entry.adjustments.flatMap(_.privateUseAdjustment),
                                        path = s"/foreignNonFhlProperty/$index/adjustments/privateUseAdjustment"),
      NumberValidation.validateOptional(field = entry.adjustments.flatMap(_.balancingCharge),
                                        path = s"/foreignNonFhlProperty/$index/adjustments/balancingCharge"),
      NumberValidation.validateOptional(field = entry.allowances.flatMap(_.annualInvestmentAllowance),
                                        path = s"/foreignNonFhlProperty/$index/allowances/annualInvestmentAllowance"),
      NumberValidation.validateOptional(field = entry.allowances.flatMap(_.costOfReplacingDomesticItems),
                                        path = s"/foreignNonFhlProperty/$index/allowances/costOfReplacingDomesticItems"),
      NumberValidation.validateOptional(
        field = entry.allowances.flatMap(_.zeroEmissionsGoodsVehicleAllowance),
        path = s"/foreignNonFhlProperty/$index/allowances/zeroEmissionsGoodsVehicleAllowance"
      ),
      NumberValidation.validateOptional(field = entry.allowances.flatMap(_.otherCapitalAllowance),
                                        path = s"/foreignNonFhlProperty/$index/allowances/otherCapitalAllowance"),
      NumberValidation.validateOptional(field = entry.allowances.flatMap(_.electricChargePointAllowance),
                                        path = s"/foreignNonFhlProperty/$index/allowances/electricChargePointAllowance"),
      NumberValidation.validateOptional(field = entry.allowances.flatMap(_.zeroEmissionsCarAllowance),
                                        path = s"/foreignNonFhlProperty/$index/allowances/zeroEmissionsCarAllowance"),
      NumberValidation.validateOptional(field = entry.allowances.flatMap(_.propertyIncomeAllowance),
                                        path = s"/foreignNonFhlProperty/$index/allowances/propertyIncomeAllowance",
                                        max = 1000),
      entry.allowances
        .flatMap(_.structuredBuildingAllowance)
        .map(_.zipWithIndex.toList.flatMap {
          case (entry, i) => validateBuilding(entry, index, i)
        })
        .getOrElse(NoValidationErrors),
      entry.allowances
        .map(allowances => AllowancesValidation.validateForeignNonFhl(allowances = allowances, path = s"/foreignNonFhlProperty/$index/allowances"))
        .getOrElse(Nil),
      validateNonFhlPropertyIncomeAllowance(entry, index)
    ).flatten
  }

  private def validateBuilding(structuredBuildingAllowance: StructuredBuildingAllowance, index: Int, bldgIdx: Int): List[MtdError] = {
    List(
      NumberValidation.validate(
        field = structuredBuildingAllowance.amount,
        path = s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$bldgIdx/amount"
      ),
      NumberValidation.validateOptional(
        field = structuredBuildingAllowance.firstYear.map(_.qualifyingAmountExpenditure),
        path = s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$bldgIdx/firstYear/qualifyingAmountExpenditure"
      ),
      StringValidation.validate(
        field = structuredBuildingAllowance.building.postcode,
        path = s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$bldgIdx/building/postcode"
      ),
      StringValidation.validateOptional(
        field = structuredBuildingAllowance.building.name,
        path = s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$bldgIdx/building/name"
      ),
      StringValidation.validateOptional(
        field = structuredBuildingAllowance.building.number,
        path = s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$bldgIdx/building/number"
      ),
      DateValidation.validateOtherDate(
        field = structuredBuildingAllowance.firstYear.map(_.qualifyingDate),
        path = s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$bldgIdx/firstYear/qualifyingDate"
      ),
      BuildingValidation.validate(
        body = structuredBuildingAllowance.building,
        path = s"/foreignNonFhlProperty/$index/allowances/structuredBuildingAllowance/$bldgIdx/building"
      ),
    ).flatten
  }

  private def validateFhlPropertyIncomeAllowance(foreignFhlEea: ForeignFhlEea): List[MtdError] = {
    (for {
      allowances  <- foreignFhlEea.allowances
      adjustments <- foreignFhlEea.adjustments
    } yield {
      (allowances.propertyIncomeAllowance, adjustments.privateUseAdjustment) match {
        case (Some(_), Some(_)) => List(RulePropertyIncomeAllowanceError.copy(paths = Some(Seq("/foreignFhlEea"))))
        case _                  => Nil
      }
    }).getOrElse(Nil)
  }

  private def validateNonFhlPropertyIncomeAllowance(foreignPropertyEntry: ForeignNonFhlEntry, index: Int): List[MtdError] = {
    (for {
      allowances  <- foreignPropertyEntry.allowances
      adjustments <- foreignPropertyEntry.adjustments
    } yield {
      (allowances.propertyIncomeAllowance, adjustments.privateUseAdjustment) match {
        case (Some(_), Some(_)) => List(RulePropertyIncomeAllowanceError.copy(paths = Some(Seq(s"/foreignNonFhlProperty/$index"))))
        case _                  => Nil
      }
    }).getOrElse(Nil)
  }

  private def duplicateCountryCodeValidation(body: CreateAmendForeignPropertyAnnualSubmissionRequestBody): List[MtdError] = {
    body.foreignNonFhlProperty
      .map { entries =>
        entries.zipWithIndex
          .map {
            case (entry, idx) => (entry.countryCode, s"/foreignNonFhlProperty/$idx/countryCode")
          }
          .groupBy(_._1)
          .collect {
            case (code, codeAndPaths) if codeAndPaths.size >= 2 =>
              RuleDuplicateCountryCodeError.forDuplicatedCodesAndPaths(code, codeAndPaths.map(_._2))
          }
          .toList
      }
      .getOrElse(Nil)
  }

  override def validate(data: CreateAmendForeignPropertyAnnualSubmissionRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
