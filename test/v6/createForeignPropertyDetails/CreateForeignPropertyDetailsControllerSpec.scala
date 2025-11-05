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

package v6.createForeignPropertyDetails

import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{BusinessId, TaxYear}
import shared.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v6.createForeignPropertyDetails.def1.model.Def1_CreateForeignPropertyDetailsFixtures
import v6.createForeignPropertyDetails.def1.model.request.Def1_CreateForeignPropertyDetailsRequestData
import v6.createForeignPropertyDetails.def1.model.response.Def1_CreateForeignPropertyDetailsResponse
import v6.createForeignPropertyDetails.model.request.CreateForeignPropertyDetailsRequestData

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CreateForeignPropertyDetailsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateForeignPropertyDetailsService
    with MockCreateForeignPropertyDetailsValidatorFactory
    with MockIdGenerator
    with Def1_CreateForeignPropertyDetailsFixtures {

  private val taxYear    = "2025-26"
  private val businessId = "XAIS12345678910"

  private val parsedBusinessId = BusinessId(businessId)
  private val parsedTaxYear    = TaxYear.fromMtd(taxYear)

  private val responseData: Def1_CreateForeignPropertyDetailsResponse = def1_CreateForeignPropertyDetailsResponseModel

  override def fakePostRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  "CreateForeignPropertyDetailsController" should {
    "return a successful response with status 200" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedCreateForeignPropertyDetailsService
          .createForeignPropertyDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(def1_CreateForeignPropertyDetailsResponseJson))

      }

      "return the error as per spec" when {
        "the parser validation fails" in new Test {
          willUseValidator(returning(NinoFormatError))

          runErrorTest(NinoFormatError)
        }

        "the service returns an error" in new Test {
          willUseValidator(returningSuccess(requestData))

          MockedCreateForeignPropertyDetailsService
            .createForeignPropertyDetails(requestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

          runErrorTest(RuleTaxYearNotSupportedError)
        }
      }
    }
  }

  trait Test extends ControllerTest {

    val controller: CreateForeignPropertyDetailsController = new CreateForeignPropertyDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateForeignPropertyDetailsValidatorFactory,
      service = mockCreateForeignPropertyDetailsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, businessId, taxYear)(fakePostRequest(requestBody))

    val requestBody: JsObject = Json.obj(
      "propertyName" -> "Bob & Bobby Co",
      "countryCode"  -> "FRA",
      "endDate"      -> "2026-08-24",
      "endReason"    -> "no-longer-renting-property-out"
    )

    protected val requestData: CreateForeignPropertyDetailsRequestData =
      Def1_CreateForeignPropertyDetailsRequestData(parsedNino, parsedBusinessId, parsedTaxYear, def1_CreateForeignPropertyDetailsModel)

  }

}
