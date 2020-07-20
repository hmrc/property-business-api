/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers

import play.api.libs.json.Json
import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v1.mocks.validators.MockAmendForeignPropertyAnnualSubmissionValidator
import v1.models.errors.{BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError}
import v1.models.request.amendForeignPropertyAnnualSubmission._
import v1.models.request.amendForeignPropertyAnnualSubmission.foreignFhlEea.{ForeignFhlEea, ForeignFhlEeaAdjustments, ForeignFhlEeaAllowances}
import v1.models.request.amendForeignPropertyAnnualSubmission.foreignProperty.{ForeignPropertyAdjustments, ForeignPropertyAllowances, ForeignPropertyEntry}

class AmendForeignPropertyAnnualSubmissionRequestParserSpec extends UnitSpec {
  val nino = "AA123456B"
  val businessId = "XAIS12345678901"
  val taxYear = "2021-22"

  private val requestBodyJson = Json.parse(
    """
      |{
      |   "foreignFhlEea":
      |      {
      |         "adjustments":{
      |            "privateUseAdjustment":100.25,
      |            "balancingCharge":100.25,
      |            "periodOfGraceAdjustment":true
      |         },
      |         "allowances":{
      |            "annualInvestmentAllowance":100.25,
      |            "otherCapitalAllowance":100.25,
      |            "propertyAllowance":100.25,
      |            "electricChargePointAllowance":100.25
      |         }
      |      },
      |   "foreignProperty":[
      |      {
      |         "countryCode":"GER",
      |         "adjustments":
      |            {
      |               "privateUseAdjustment":100.25,
      |               "balancingCharge":100.25
      |            }
      |         ,
      |         "allowances":{
      |            "annualInvestmentAllowance":100.25,
      |            "costOfReplacingDomesticItems":100.25,
      |            "zeroEmissionsGoodsVehicleAllowance":100.25,
      |            "propertyAllowance":100.25,
      |            "otherCapitalAllowance":100.25,
      |            "structureAndBuildingAllowance":100.25,
      |            "electricChargePointAllowance":100.25
      |         }
      |      }
      |   ]
      |}
      |""".stripMargin
  )

  val inputData =
    AmendForeignPropertyAnnualSubmissionRawData(nino, businessId, taxYear, requestBodyJson)

  trait Test extends MockAmendForeignPropertyAnnualSubmissionValidator {
    lazy val parser = new AmendForeignPropertyAnnualSubmissionRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendForeignPropertyValidator.validate(inputData).returns(Nil)

        val amendForeignPropertyAnnualSubmissionRequestBody =
          AmendForeignPropertyAnnualSubmissionRequestBody(
            Some(ForeignFhlEea(
              Some(ForeignFhlEeaAdjustments(
                Some(100.25),
                Some(100.25),
                Some(true)
              )),
              Some(ForeignFhlEeaAllowances(
                Some(100.25),
                Some(100.25),
                Some(100.25),
                Some(100.25)
              ))
            )),
            Some(Seq(ForeignPropertyEntry(
              "GER",
              Some(ForeignPropertyAdjustments(
                Some(100.25),
                Some(100.25))),
              Some(ForeignPropertyAllowances(
                Some(100.25),
                Some(100.25),
                Some(100.25),
                Some(100.25),
                Some(100.25),
                Some(100.25),
                Some(100.25)
              )))))
          )

        parser.parseRequest(inputData) shouldBe
          Right(AmendForeignPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear, amendForeignPropertyAnnualSubmissionRequestBody))
      }
    }
    "return an ErrroWrapper" when {
      "a single validation error occurs" in new Test {
        MockAmendForeignPropertyValidator.validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendForeignPropertyValidator.validate(inputData)
          .returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }
}
