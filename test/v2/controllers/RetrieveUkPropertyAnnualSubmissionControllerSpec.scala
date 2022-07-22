/*
 * Copyright 2022 HM Revenue & Customs
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

package v2.controllers

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockRetrieveUkPropertyAnnualSubmissionRequestParser
import v2.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveUkPropertyAnnualSubmissionService}
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.hateoas.Method.GET
import v2.models.hateoas.{HateoasWrapper, Link}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveUkPropertyAnnualSubmission._
import v2.models.response.retrieveUkPropertyAnnualSubmission._
import v2.models.response.retrieveUkPropertyAnnualSubmission.ukFhlProperty._
import v2.models.response.retrieveUkPropertyAnnualSubmission.ukNonFhlProperty._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUkPropertyAnnualSubmissionControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveUkPropertyAnnualSubmissionService
    with MockRetrieveUkPropertyAnnualSubmissionRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val nino = "AA123456A"
  private val businessId = "XAIS12345678910"
  private val taxYear = "2020-21"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveUkPropertyAnnualSubmissionRequestParser,
      service = mockRetrieveUkPropertyAnnualSubmissionService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val rawData = RetrieveUkPropertyAnnualSubmissionRawData(nino, businessId, taxYear)
  private val requestData = RetrieveUkPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear)

  private val testHateoasLink = Link(href = s"Individuals/business/property/uk/$nino/$businessId/annual/$taxYear", method = GET, rel = "self")

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
          )),
      )
    ),
    allowances = Some(
      UkFhlPropertyAllowances(
        Some(123.45),
        Some(345.56),
        Some(345.34),
        Some(453.45),
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
        propertyIncomeAllowance = Some(342.34),
        electricChargePointAllowance = Some(454.34),
        structuredBuildingAllowance = Some(
          Seq(
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
          Seq(
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

  val responseBody: RetrieveUkPropertyAnnualSubmissionResponse = RetrieveUkPropertyAnnualSubmissionResponse(
    submittedOn = "2020-06-17T10:53:38Z", ukFhlProperty = Some(ukFhlProperty), ukNonFhlProperty = Some(ukNonFhlProperty)
  )

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockRetrieveUkPropertyRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveUkPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(responseBody, RetrieveUkPropertyAnnualSubmissionHateoasData(nino, businessId, taxYear))
          .returns(HateoasWrapper(responseBody, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequest)
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return an error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveUkPropertyRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }
      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveUkPropertyRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockRetrieveUkPropertyService
              .retrieve(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTypeOfBusinessIncorrectError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (InternalError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
