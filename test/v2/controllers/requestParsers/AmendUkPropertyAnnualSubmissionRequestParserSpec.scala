/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.mocks.validators.MockAmendUkPropertyAnnualSubmissionValidator
import v2.models.domain.Nino
import v2.models.errors.{BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError}
import v2.models.request.amendUkPropertyAnnualSubmission.ukFhlProperty.{UkFhlProperty, UkFhlPropertyAdjustments, UkFhlPropertyAllowances}
import v2.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty.{Building, FirstYear, StructuredBuildingAllowance, UkNonFhlProperty, UkNonFhlPropertyAdjustments, UkNonFhlPropertyAllowances}
import v2.models.request.amendUkPropertyAnnualSubmission.{AmendUkPropertyAnnualSubmissionRawData, AmendUkPropertyAnnualSubmissionRequest, AmendUkPropertyAnnualSubmissionRequestBody}
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom

class AmendUkPropertyAnnualSubmissionRequestParserSpec extends UnitSpec {

  val nino: String = "AA123456B"
  val businessId: String = "XAIS12345678901"
  val taxYear: String = "2021-22"
  implicit val correlationId: String = "X-123"

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "ukFhlProperty": {
      |    "allowances": {
      |      "annualInvestmentAllowance": 123.45,
      |      "businessPremisesRenovationAllowance": 345.56,
      |      "otherCapitalAllowance": 345.34,
      |      "electricChargePointAllowance": 453.34,
      |      "zeroEmissionsCarAllowance": 123.12
      |    },
      |    "adjustments": {
      |      "lossBroughtForward": 343.34,
      |      "privateUseAdjustment": 454.45,
      |      "balancingCharge": 231.45,
      |      "periodOfGraceAdjustment": true,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 567.67,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |        "jointlyLet": true
      |      }
      |    }
      |  },
      |  "ukNonFhlProperty": {
      |    "allowances": {
      |      "annualInvestmentAllowance": 678.45,
      |      "zeroEmissionGoodsVehicleAllowance": 456.34,
      |      "businessPremisesRenovationAllowance": 573.45,
      |      "otherCapitalAllowance": 452.34,
      |      "costOfReplacingDomesticGoods": 567.34,
      |      "electricChargePointAllowance": 454.34,
      |      "structuredBuildingAllowance": [
      |        {
      |          "amount": 234.34,
      |          "firstYear": {
      |            "qualifyingDate": "2020-03-29",
      |            "qualifyingAmountExpenditure": 3434.45
      |          },
      |          "building": {
      |            "name": "Plaza",
      |            "number": "1",
      |            "postcode": "TF3 4EH"
      |          }
      |        }
      |      ],
      |      "enhancedStructuredBuildingAllowance": [
      |        {
      |          "amount": 234.45,
      |          "firstYear": {
      |            "qualifyingDate": "2020-05-29",
      |            "qualifyingAmountExpenditure": 453.34
      |          },
      |          "building": {
      |            "name": "Plaza 2",
      |            "number": "2",
      |            "postcode": "TF3 4ER"
      |          }
      |        }
      |      ],
      |      "zeroEmissionsCarAllowance": 454.34
      |    },
      |    "adjustments": {
      |      "lossBroughtForward": 334.45,
      |      "balancingCharge": 565.34,
      |      "privateUseAdjustment": 533.54,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 563.34,
      |      "nonResidentLandlord": true,
      |      "rentARoom": {
      |        "jointlyLet": true
      |      }
      |    }
      |  }
      |}
      |""".stripMargin)

  val inputData: AmendUkPropertyAnnualSubmissionRawData =
    AmendUkPropertyAnnualSubmissionRawData(nino, businessId, taxYear, requestBodyJson)

  trait Test extends MockAmendUkPropertyAnnualSubmissionValidator {
    lazy val parser = new AmendUkPropertyAnnualSubmissionRequestParser(mockValidator)
  }
  
  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendUkPropertyValidator.validate(inputData).returns(Nil)

        val amendUkPropertyAnnualSubmissionRequestBody: AmendUkPropertyAnnualSubmissionRequestBody =
          AmendUkPropertyAnnualSubmissionRequestBody(
            Some(UkFhlProperty(
              Some(UkFhlPropertyAdjustments(
                Some(1000.10),
                Some(1000.20),
                Some(1000.30),
                true,
                Some(1000.40),
                true,
                Some(UkPropertyAdjustmentsRentARoom(true))
              )),
              Some(UkFhlPropertyAllowances(
                Some(1000.50),
                Some(1000.60),
                Some(1000.70),
                Some(1000.80),
                Some(1000.90),
                None
              ))
            )),
            Some(UkNonFhlProperty(
              Some(UkNonFhlPropertyAdjustments(
                Some(2000.10),
                Some(2000.20),
                Some(2000.30),
                Some(2000.40),
                true,
                Some(UkPropertyAdjustmentsRentARoom(true))
              )),
              Some(UkNonFhlPropertyAllowances(
                Some(2000.50),
                Some(2000.60),
                Some(2000.70),
                Some(2000.80),
                Some(2000.90),
                Some(3000.10),
                Some(3000.20),
                None,
                Some(Seq(StructuredBuildingAllowance(
                  3000.30,
                  Some(FirstYear(
                    "2020-01-01",
                    3000.40
                  )),
                  Building(
                    Some("house name"),
                    None,
                    "GF49JH"
                  )
                ))),
                Some(Seq(StructuredBuildingAllowance(
                  3000.50,
                  Some(FirstYear(
                    "2020-01-01",
                    3000.60
                  )),
                  Building(
                    None,
                    Some("house number"),
                    "GF49JH"
                  )
                )))
              ))
            ))
          )

        parser.parseRequest(inputData) shouldBe
          Right(AmendUkPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear, amendUkPropertyAnnualSubmissionRequestBody))
      }
    }
    "return an ErrrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockAmendUkPropertyValidator.validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendUkPropertyValidator.validate(inputData)
          .returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }
}