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

package v6.createAmendHistoricFhlUkPropertyAnnualSubmission.def1

import common.models.errors.RuleHistoricTaxYearNotSupportedError
import config.MockPropertyBusinessConfig
import play.api.libs.json.{JsNumber, JsObject, JsValue, Json}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v6.createAmendHistoricFhlUkPropertyAnnualSubmission.def1.model.request._
import v6.createAmendHistoricFhlUkPropertyAnnualSubmission.model.request._

class Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionValidatorSpec extends UnitSpec with MockPropertyBusinessConfig with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2019-20"

  private val validRequestBody = Json.parse(
    """
      |  {
      |     "annualAdjustments": {
      |        "lossBroughtForward": 111.50,
      |        "privateUseAdjustment": 222.00,
      |        "balancingCharge": 333.00,
      |        "periodOfGraceAdjustment": true,
      |        "businessPremisesRenovationAllowanceBalancingCharges": 444.00,
      |        "nonResidentLandlord": false,
      |        "rentARoom": {
      |           "jointlyLet": true
      |        }
      |     },
      |     "annualAllowances": {
      |        "annualInvestmentAllowance": 111.00,
      |        "businessPremisesRenovationAllowance": 222.00,
      |        "otherCapitalAllowance": 333.00
      |     }
      |  }
      |""".stripMargin
  )

  private val validRequestBodyWithoutAnnualAllowances = Json.parse("""
     | {
     |   "annualAdjustments": {
     |        "lossBroughtForward": 111.50,
     |        "privateUseAdjustment": 222.00,
     |        "balancingCharge": 333.00,
     |        "periodOfGraceAdjustment": true,
     |        "businessPremisesRenovationAllowanceBalancingCharges": 444.00,
     |        "nonResidentLandlord": false,
     |        "rentARoom": {
     |           "jointlyLet": true
     |        }
     |   }
     | }
     |""".stripMargin)

  private val validRequestBodyWithoutAnnualAdjustments = Json.parse(
    """
      |  {
      |     "annualAllowances": {
      |        "annualInvestmentAllowance": 111.00,
      |        "businessPremisesRenovationAllowance": 222.00,
      |        "otherCapitalAllowance": 333.00
      |     }
      |  }
      |""".stripMargin
  )

  private val incompleteRequestBody = Json.parse("""
     | {
     |   "annualAdjustments": {
     |      "lossBroughtForward": 200.00,
     |      "balancingCharge": 200.00,
     |      "privateUseAdjustment": 200.00,
     |      "periodOfGraceAdjustment-MISSING-BECAUSE-MISSPELT": true,
     |      "businessPremisesRenovationAllowanceBalancingCharges": 200.02,
     |      "nonResidentLandlord": true,
     |      "rentARoom": {
     |         "jointlyLet": true
     |      }
     |   }
     | }
     |""".stripMargin)

  private val requestBodyWithInvalidAmounts = Json.parse("""
     | {
     |   "annualAdjustments": {
     |      "lossBroughtForward": 200.123,
     |      "balancingCharge": -1.00,
     |      "privateUseAdjustment": 999999999990.99,
     |      "periodOfGraceAdjustment": true,
     |      "businessPremisesRenovationAllowanceBalancingCharges": 200.02,
     |      "nonResidentLandlord": true,
     |      "rentARoom": {
     |         "jointlyLet": true
     |      }
     |   }
     | }
     |""".stripMargin)

  private val requestBodyWithEmptySubObjects = Json.parse("""
    | {
    |   "annualAdjustments": {
    |   },
    |   "annualAllowances": {
    |   }
    | }
    |""".stripMargin)

  private val requestBodyWithEmptyRentARoom = Json.parse("""
     | {
     |   "annualAdjustments": {
     |      "lossBroughtForward": 200.00,
     |      "balancingCharge": 200.00,
     |      "privateUseAdjustment": 200.00,
     |      "periodOfGraceAdjustment": true,
     |      "businessPremisesRenovationAllowanceBalancingCharges": 200.02,
     |      "nonResidentLandlord": true,
     |      "rentARoom": {
     |      }
     |   }
     | }
     |""".stripMargin)

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private def number(n: String): Option[BigDecimal] = Option(BigDecimal(n))

  private val historicFhlAnnualAdjustments = HistoricFhlAnnualAdjustments(
    number("111.50"),
    number("222.00"),
    number("333.00"),
    periodOfGraceAdjustment = true,
    number("444.00"),
    nonResidentLandlord = false,
    Some(UkPropertyAdjustmentsRentARoom(true))
  )

  private val historicFhlAnnualAllowances = HistoricFhlAnnualAllowances(number("111.00"), number("222.00"), number("333.00"), None)

  private val parsedBody =
    Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody(Some(historicFhlAnnualAdjustments), Some(historicFhlAnnualAllowances))

  private def validator(nino: String, taxYear: String, body: JsValue) =
    new Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionValidator(nino, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validRequestBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData(parsedNino, parsedTaxYear, parsedBody))
      }

      "passed a valid request that is missing the optional AnnualAllowances object" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validRequestBodyWithoutAnnualAllowances).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData(parsedNino, parsedTaxYear, parsedBody.copy(annualAllowances = None)))
      }

      "passed a valid request that is missing the optional AnnualAdjustments object" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validRequestBodyWithoutAnnualAdjustments).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData(parsedNino, parsedTaxYear, parsedBody.copy(annualAdjustments = None)))
      }

      "passed the minimum supported taxYear" in new SetupConfig { allowsTaxYear("2017-18") }
      "passed the maximum supported taxYear" in new SetupConfig { allowsTaxYear("2021-22") }

      def allowsTaxYear(taxYearString: String): Unit = {
        validator(validNino, taxYearString, validRequestBody).validateAndWrapResult() shouldBe
          Right(Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData(parsedNino, TaxYear.fromMtd(taxYearString), parsedBody))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator("invalid nino", validTaxYear, validRequestBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an invalid tax year" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "invalid", validRequestBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed a tax year with an invalid range" in new SetupConfig {

        val result: Either[ErrorWrapper, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, "2019-21", validRequestBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed a taxYear immediately before the minimum supported" in new SetupConfig { disallowsTaxYear("2016-17") }
      "passed a taxYear immediately after the maximum supported" in new SetupConfig { disallowsTaxYear("2022-23") }

      def disallowsTaxYear(taxYearString: String): Unit = {
        validator(validNino, taxYearString, validRequestBody).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleHistoricTaxYearNotSupportedError))
      }

      "passed a request body with a mandatory field missing" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, incompleteRequestBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/annualAdjustments/periodOfGraceAdjustment")))
      }

      "passed a request body with multiple invalid numeric amounts" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, requestBodyWithInvalidAmounts).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(
              List("/annualAdjustments/lossBroughtForward", "/annualAdjustments/privateUseAdjustment", "/annualAdjustments/balancingCharge")
            )
          ))
      }

      "passed a request body with a propertyIncomeAllowance of over 1000" in new SetupConfig {
        val maxValue: BigDecimal = 1000.00

        val result: Either[ErrorWrapper, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, validRequestBody.update("/annualAllowances/propertyIncomeAllowance", JsNumber(1000.01)))
            .validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.forPathAndRange("/annualAllowances/propertyIncomeAllowance", min = "0", max = maxValue.toString())))
      }

      "passed an empty request body" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a request body with empty annualAdjustments and annualAllowances sub-objects" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, requestBodyWithEmptySubObjects).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleIncorrectOrEmptyBodyError.withPaths(List("/annualAdjustments/nonResidentLandlord", "/annualAdjustments/periodOfGraceAdjustment")))
        )
      }

      "passed a request body with an empty rentARoom sub-object" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator(validNino, validTaxYear, requestBodyWithEmptyRentARoom).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleIncorrectOrEmptyBodyError.withPaths(List("/annualAdjustments/rentARoom/jointlyLet"))
          ))
      }

    }

    "return multiple errors" when {
      "the path parameters have multiple issues" in new SetupConfig {
        val result: Either[ErrorWrapper, CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validator("invalid", "invalid", validRequestBody).validateAndWrapResult()

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
