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

package v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission.def1

import common.models.errors.RuleHistoricTaxYearNotSupportedError
import config.MockPropertyBusinessConfig
import play.api.libs.json._
import shared.controllers.validators.Validator
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission.def1.model.request._
import v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission.model.request._

class Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSummaryValidatorSpec extends UnitSpec with MockPropertyBusinessConfig with JsonErrorValidators {

  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2019-20"

  private val validBody = Json.parse("""
     |{
     |   "annualAdjustments": {
     |      "lossBroughtForward": 200.00,
     |      "privateUseAdjustment": 201.00,
     |      "balancingCharge": 202.00,
     |      "businessPremisesRenovationAllowanceBalancingCharges": 203.00,
     |      "nonResidentLandlord": true,
     |      "rentARoom": {
     |         "jointlyLet": true
     |      }
     |   },
     |   "annualAllowances": {
     |      "annualInvestmentAllowance": 204.00,
     |      "zeroEmissionGoodsVehicleAllowance": 205.00,
     |      "businessPremisesRenovationAllowance": 206.00,
     |      "otherCapitalAllowance": 207.00,
     |      "costOfReplacingDomesticGoods": 208.00,
     |      "propertyIncomeAllowance": 209.00
     |   }
     |}
     |""".stripMargin)

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val parsedUkPropertyAdjustmentsRentARoom = UkPropertyAdjustmentsRentARoom(true)

  //@formatter:off
  private val parsedAnnualAdjustments =
    HistoricNonFhlAnnualAdjustments(
      Some(200.00), Some(201.00), Some(202.00), Some(203.00),
      nonResidentLandlord = true, Some(parsedUkPropertyAdjustmentsRentARoom)
    )

  private val parsedAnnualAllowances = HistoricNonFhlAnnualAllowances(
    Some(204.00), Some(205.00), Some(206.00),
    Some(207.00), Some(208.00), Some(209.00)
  )
  //@formatter:on

  private val parsedBody = Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody(
    Some(parsedAnnualAdjustments),
    Some(parsedAnnualAllowances)
  )

  private def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
    new Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator(nino, taxYear, body)

  "validate()" should {
    "return the parsed domain object" when {
      "given a valid request" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData(parsedNino, parsedTaxYear, parsedBody)
        )
      }

      "given a valid request with only annualAdjustments" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBody.removeProperty("/annualAllowances")).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData(parsedNino, parsedTaxYear, parsedBody.copy(annualAllowances = None))
        )
      }

      "given a valid request with only annualAllowances" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validBody.removeProperty("/annualAdjustments")).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData(parsedNino, parsedTaxYear, parsedBody.copy(annualAdjustments = None))
        )
      }

      "given the minimum supported taxYear" in new SetupConfig { allowsTaxYear("2017-18") }
      "given the maximum supported taxYear" in new SetupConfig { allowsTaxYear("2021-22") }

      def allowsTaxYear(taxYearString: String): Unit = {

        val result = validator(validNino, taxYearString, validBody).validateAndWrapResult()
        result shouldBe Right(
          Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData(parsedNino, TaxYear.fromMtd(taxYearString), parsedBody)
        )
      }
    }

    "return a single error" when {
      "given an invalid nino" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator("invalid nino", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given an invalidly formatted taxYear" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "given a taxYear immediately before the minimum supported" in new SetupConfig { disallowsTaxYear("2016-17") }
      "given a taxYear immediately after the maximum supported" in new SetupConfig { disallowsTaxYear("2022-23") }

      def disallowsTaxYear(taxYearString: String): Unit = {

        val result = validator(validNino, taxYearString, validBody).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleHistoricTaxYearNotSupportedError))
      }

      "given a taxYear with an invalid range" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "2020-22", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "given a body with an invalid amount" when {
        def testWith(path: String, min: BigDecimal, max: BigDecimal): Unit = s"for $path" when {
          val expected = Left(ErrorWrapper(correlationId, ValueFormatError.forPathAndRange(path, min.toString, max.toString)))

          "it is too small" in new SetupConfig {

            val invalidBody = validBody.update(path, JsNumber(min - 0.01))
            val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
              validator(validNino, "2020-21", invalidBody).validateAndWrapResult()

            result shouldBe expected
          }

          "it is too big" in new SetupConfig {

            val invalidBody = validBody.update(path, JsNumber(max + 0.01))
            val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
              validator(validNino, "2020-21", invalidBody).validateAndWrapResult()

            result shouldBe expected
          }

          "it is a bad value" in new SetupConfig {

            val invalidBody = validBody.update(path, JsNumber(123.456))
            val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
              validator(validNino, "2020-21", invalidBody).validateAndWrapResult()

            result shouldBe expected
          }
        }

        val max: BigDecimal  = 99999999999.99
        val zero: BigDecimal = 0

        List(
          ("/annualAdjustments/lossBroughtForward", zero, max),
          ("/annualAdjustments/privateUseAdjustment", zero, max),
          ("/annualAdjustments/balancingCharge", zero, max),
          ("/annualAdjustments/businessPremisesRenovationAllowanceBalancingCharges", zero, max),
          ("/annualAllowances/annualInvestmentAllowance", zero, max),
          ("/annualAllowances/zeroEmissionGoodsVehicleAllowance", zero, max),
          ("/annualAllowances/businessPremisesRenovationAllowance", zero, max),
          ("/annualAllowances/otherCapitalAllowance", zero, max),
          ("/annualAllowances/costOfReplacingDomesticGoods", zero, max),
          ("/annualAllowances/propertyIncomeAllowance", zero, 1000: BigDecimal)
        ).foreach((testWith _).tupled)
      }

      "given an empty body" in new SetupConfig {

        val invalidBody = JsObject.empty
        val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "given a body with a missing mandatory field" in new SetupConfig {

        val invalidBody = validBody.removeProperty("/annualAdjustments/nonResidentLandlord")
        val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/annualAdjustments/nonResidentLandlord")))
      }

      "given a body with an annualAdjustments/nonResidentLandlord field containing an empty object" in new SetupConfig {

        val invalidBody = validBody.replaceWithEmptyObject("/annualAdjustments/nonResidentLandlord")
        val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/annualAdjustments/nonResidentLandlord")))
      }

      "given a body with an annualAdjustments field containing an empty object" in new SetupConfig {

        val invalidBody = validBody.replaceWithEmptyObject("/annualAdjustments")
        val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/annualAdjustments/nonResidentLandlord")))
      }

      "given a body with an annualAllowances field containing an empty object" in new SetupConfig {

        val invalidBody = validBody.replaceWithEmptyObject("/annualAllowances")
        val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/annualAllowances")))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
          validator("invalid", "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, TaxYearFormatError))
          )
        )
      }
    }
  }

}
