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

package v3.controllers

import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import shared.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import shared.hateoas.Method.GET
import v3.controllers.validators.MockRetrieveUkPropertyAnnualSubmissionValidatorFactory
import v3.models.request.retrieveUkPropertyAnnualSubmission._
import v3.models.response.retrieveUkPropertyAnnualSubmission._
import v3.models.response.retrieveUkPropertyAnnualSubmission.ukFhlProperty._
import v3.models.response.retrieveUkPropertyAnnualSubmission.ukNonFhlProperty._
import v3.services.MockRetrieveUkPropertyAnnualSubmissionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockRetrieveUkPropertyAnnualSubmissionService
    with MockRetrieveUkPropertyAnnualSubmissionValidatorFactory
    with MockHateoasFactory {

  private val businessId          = "XAIS12345678910"
  private val taxYear             = "2020-21"
  val testHateoasLinks: Seq[Link] = List(Link(href = "/some/link", method = GET, rel = "someRel"))

  val testHateoasLinksJson: JsObject = Json
    .parse("""{
        |  "links": [ { "href":"/some/link", "method":"GET", "rel":"someRel" } ]
        |}
        |""".stripMargin)
    .as[JsObject]

  "RetrieveUkPropertyAnnualSubmissionController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveUkPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, hateoasData)
          .returns(HateoasWrapper(responseData, testHateoasLinks))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseBodyJsonWithHateoas))
      }
    }

    "return an error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(expectedError = NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveUkPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(expectedError = RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    protected val controller = new RetrieveUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveUkPropertyAnnualSubmissionValidatorFactory,
      service = mockRetrieveUkPropertyAnnualSubmissionService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, businessId, taxYear)(fakeGetRequest)

    protected val requestData: RetrieveUkPropertyAnnualSubmissionRequestData =
      RetrieveUkPropertyAnnualSubmissionRequestData(Nino(validNino), BusinessId(businessId), TaxYear.fromMtd(taxYear))

    private val ukFhlProperty = UkFhlProperty(
      adjustments = Some(
        UkFhlPropertyAdjustments(
          privateUseAdjustment = Some(454.45),
          balancingCharge = Some(231.45),
          periodOfGraceAdjustment = true,
          businessPremisesRenovationAllowanceBalancingCharges = Some(567.67),
          nonResidentLandlord = true,
          rentARoom = Some(
            UkFhlPropertyRentARoom(
              jointlyLet = true
            ))
        )
      ),
      allowances = Some(
        UkFhlPropertyAllowances(
          Some(123.45),
          Some(345.56),
          Some(345.34),
          None,
          Some(453.34),
          Some(123.12)
        )
      )
    )

    private val ukNonFhlProperty = UkNonFhlProperty(
      adjustments = Some(
        UkNonFhlPropertyAdjustments(
          balancingCharge = Some(565.34),
          privateUseAdjustment = Some(533.54),
          businessPremisesRenovationAllowanceBalancingCharges = Some(563.34),
          nonResidentLandlord = true,
          rentARoom = Some(
            UkNonFhlPropertyRentARoom(
              jointlyLet = true
            ))
        )
      ),
      allowances = Some(
        UkNonFhlPropertyAllowances(
          annualInvestmentAllowance = Some(678.45),
          zeroEmissionsGoodsVehicleAllowance = Some(456.34),
          businessPremisesRenovationAllowance = Some(573.45),
          otherCapitalAllowance = Some(452.34),
          costOfReplacingDomesticGoods = Some(567.34),
          propertyIncomeAllowance = None,
          electricChargePointAllowance = Some(454.34),
          structuredBuildingAllowance = Some(
            List(
              UkNonFhlPropertyStructuredBuildingAllowance(
                amount = 234.34,
                firstYear = Some(
                  UkNonFhlPropertyFirstYear(
                    qualifyingDate = "2020-03-29",
                    qualifyingAmountExpenditure = 3434.45
                  )
                ),
                building = UkNonFhlPropertyBuilding(
                  name = Some("Plaza"),
                  number = Some("1"),
                  postcode = "TF3 4EH"
                )
              )
            )),
          enhancedStructuredBuildingAllowance = Some(
            List(
              UkNonFhlPropertyStructuredBuildingAllowance(
                amount = 234.45,
                firstYear = Some(
                  UkNonFhlPropertyFirstYear(
                    qualifyingDate = "2020-05-29",
                    qualifyingAmountExpenditure = 453.34
                  )
                ),
                building = UkNonFhlPropertyBuilding(
                  name = Some("Plaza 2"),
                  number = Some("2"),
                  postcode = "TF3 4ER"
                )
              )
            )),
          zeroEmissionsCarAllowance = Some(454.34)
        )
      )
    )

    protected val responseData: RetrieveUkPropertyAnnualSubmissionResponse = RetrieveUkPropertyAnnualSubmissionResponse(
      submittedOn = Timestamp("2020-06-17T10:53:38.000Z"),
      ukFhlProperty = Some(ukFhlProperty),
      ukNonFhlProperty = Some(ukNonFhlProperty)
    )

    protected val hateoasData: RetrieveUkPropertyAnnualSubmissionHateoasData =
      RetrieveUkPropertyAnnualSubmissionHateoasData(validNino, businessId, taxYear)

    protected val responseBodyJson: JsValue = Json.parse(
      """
        |{
        |   "submittedOn":"2020-06-17T10:53:38.000Z",
        |   "ukFhlProperty":{
        |      "adjustments":{
        |         "privateUseAdjustment":454.45,
        |         "balancingCharge":231.45,
        |         "periodOfGraceAdjustment":true,
        |         "businessPremisesRenovationAllowanceBalancingCharges":567.67,
        |         "nonResidentLandlord":true,
        |         "rentARoom":{
        |            "jointlyLet":true
        |         }
        |      },
        |      "allowances":{
        |         "annualInvestmentAllowance":123.45,
        |         "businessPremisesRenovationAllowance":345.56,
        |         "otherCapitalAllowance":345.34,
        |         "electricChargePointAllowance":453.34,
        |         "zeroEmissionsCarAllowance":123.12
        |      }
        |   },
        |   "ukNonFhlProperty":{
        |      "adjustments":{
        |         "balancingCharge":565.34,
        |         "privateUseAdjustment":533.54,
        |         "businessPremisesRenovationAllowanceBalancingCharges":563.34,
        |         "nonResidentLandlord":true,
        |         "rentARoom":{
        |            "jointlyLet":true
        |         }
        |      },
        |      "allowances":{
        |         "annualInvestmentAllowance":678.45,
        |         "zeroEmissionsGoodsVehicleAllowance":456.34,
        |         "businessPremisesRenovationAllowance":573.45,
        |         "otherCapitalAllowance":452.34,
        |         "costOfReplacingDomesticGoods":567.34,
        |         "electricChargePointAllowance":454.34,
        |         "structuredBuildingAllowance":[
        |            {
        |               "amount":234.34,
        |               "firstYear":{
        |                  "qualifyingDate":"2020-03-29",
        |                  "qualifyingAmountExpenditure":3434.45
        |               },
        |               "building":{
        |                  "name":"Plaza",
        |                  "number":"1",
        |                  "postcode":"TF3 4EH"
        |               }
        |            }
        |         ],
        |         "enhancedStructuredBuildingAllowance":[
        |            {
        |               "amount":234.45,
        |               "firstYear":{
        |                  "qualifyingDate":"2020-05-29",
        |                  "qualifyingAmountExpenditure":453.34
        |               },
        |               "building":{
        |                  "name":"Plaza 2",
        |                  "number":"2",
        |                  "postcode":"TF3 4ER"
        |               }
        |            }
        |         ],
        |         "zeroEmissionsCarAllowance":454.34
        |      }
        |   }
        |}
    """.stripMargin
    )

    protected val responseBodyJsonWithHateoas: JsObject = responseBodyJson.as[JsObject] ++ testHateoasLinksJson

  }

}
