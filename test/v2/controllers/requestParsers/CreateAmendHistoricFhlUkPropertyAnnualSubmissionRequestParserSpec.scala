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
import v2.mocks.validators.MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionValidator
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom
import v2.models.request.createAmendHistoricFhlUkPropertyAnnualSubmission._

class CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val mtdTaxYear: String             = "2022-23"
  val taxYear: TaxYear               = TaxYear.fromMtd(mtdTaxYear)
  implicit val correlationId: String = "X-123"

  "The request parser" should {
    "return a parsed request object" when {
      "given valid request data" in new Test {
        MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionValidator.validate(rawData).returns(Nil)

        parser.parseRequest(rawData) shouldBe Right(request)
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionValidator
          .validate(rawData)
          .returns(List(NinoFormatError))

        parser.parseRequest(rawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionValidator
          .validate(rawData)
          .returns(List(NinoFormatError, DateFormatError))

        parser.parseRequest(rawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, DateFormatError))))
      }
    }
  }

  trait Test extends MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionValidator {
    lazy val parser = new CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestParser(mockValidator)

    protected val requestBodyJson: JsValue = Json.parse(
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

    protected def number(n: String): Option[BigDecimal] = Option(BigDecimal(n))

    protected val rawData: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData =
      CreateAmendHistoricFhlUkPropertyAnnualSubmissionRawData(nino, mtdTaxYear, requestBodyJson)

    protected val requestBody: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody =
      CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody(
        Some(
          HistoricFhlAnnualAdjustments(
            number("111.50"),
            number("222.00"),
            number("333.00"),
            periodOfGraceAdjustment = true,
            number("444.00"),
            nonResidentLandlord = false,
            Some(UkPropertyAdjustmentsRentARoom(true))
          )),
        Some(HistoricFhlAnnualAllowances(number("111.00"), number("222.00"), number("333.00"), None))
      )

    protected val request: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequest =
      CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequest(Nino(nino), taxYear, requestBody)

  }

}
