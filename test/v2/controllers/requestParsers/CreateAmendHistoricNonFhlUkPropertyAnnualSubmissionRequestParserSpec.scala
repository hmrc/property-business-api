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

package v2.controllers.requestParsers

import api.models.domain.{Nino, TaxYear}
import api.models.errors.{BadRequestError, DateFormatError, ErrorWrapper, NinoFormatError}
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.mocks.validators.MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom
import v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission._

class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParserSpec extends UnitSpec {

  val nino: String       = "AA123456B"
  val mtdTaxYear: String = "2022-23"
  val taxYear: TaxYear   = TaxYear.fromMtd(mtdTaxYear)

  implicit val correlationId: String = "X-123"

  "The request parser" should {
    "return a parsed request object" when {
      "given valid request data" in new Test {
        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator.validate(rawData).returns(Nil)

        parser.parseRequest(rawData) shouldBe
          Right(request)
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator
          .validate(rawData)
          .returns(List(NinoFormatError))

        parser.parseRequest(rawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator
          .validate(rawData)
          .returns(List(NinoFormatError, DateFormatError))

        parser.parseRequest(rawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, DateFormatError))))
      }
    }

  }

  trait Test extends MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator {
    lazy val parser = new CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser(mockValidator)

    protected val requestBodyJson: JsValue = Json.parse("""
      |{
      |   "annualAdjustments": {
      |      "lossBroughtForward": 100.00,
      |      "privateUseAdjustment": 200.00,
      |      "balancingCharge": 300.00,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 400.00,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |         "jointlyLet": true
      |      }
      |   },
      |   "annualAllowances": {
      |      "annualInvestmentAllowance": 500.00,
      |      "zeroEmissionGoodsVehicleAllowance": 600.00,
      |      "businessPremisesRenovationAllowance": 700.00,
      |      "otherCapitalAllowance": 800.00,
      |      "costOfReplacingDomesticGoods": 900.00,
      |      "propertyIncomeAllowance": 1000.00
      |   }
      |}
      |""".stripMargin)

    protected val rawData: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData =
      CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData(nino, mtdTaxYear, requestBodyJson)

    private val annualAdjustments: HistoricNonFhlAnnualAdjustments =
      HistoricNonFhlAnnualAdjustments(
        lossBroughtForward = Some(100.00),
        privateUseAdjustment = Some(200.00),
        balancingCharge = Some(300.00),
        businessPremisesRenovationAllowanceBalancingCharges = Some(400.00),
        nonResidentLandlord = true,
        rentARoom = Some(UkPropertyAdjustmentsRentARoom(true))
      )

    private val annualAllowances: HistoricNonFhlAnnualAllowances =
      HistoricNonFhlAnnualAllowances(
        annualInvestmentAllowance = Some(500.00),
        zeroEmissionGoodsVehicleAllowance = Some(600.00),
        businessPremisesRenovationAllowance = Some(700.00),
        otherCapitalAllowance = Some(800.00),
        costOfReplacingDomesticGoods = Some(900.00),
        propertyIncomeAllowance = Some(1000.00)
      )

    protected val requestBody: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody =
      CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody(
        Some(annualAdjustments),
        Some(annualAllowances)
      )

    protected val request: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest =
      CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest(Nino(nino), taxYear, requestBody)

  }

}
