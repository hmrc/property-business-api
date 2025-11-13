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

package v6.retrieveForeignPropertyDetails

import common.models.domain.PropertyId
import play.api.Configuration
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import v6.retrieveForeignPropertyDetails.def1.model.Def1_RetrieveForeignPropertyDetailsFixture
import v6.retrieveForeignPropertyDetails.def1.model.request.Def1_RetrieveForeignPropertyDetailsRequestData
import v6.retrieveForeignPropertyDetails.model.request.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyDetailsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveForeignPropertyDetailsService
    with MockRetrieveForeignPropertyDetailsValidatorFactory
    with Def1_RetrieveForeignPropertyDetailsFixture {

  private val businessId = BusinessId("XAIS12345678910")
  private val taxYear    = TaxYear.fromMtd("2022-23")
  private val propertyId = PropertyId("8e8b8450-dc1b-4360-8109-7067337b42cb")

  "RetrieveForeignPropertyDetailsController" should {
    "return (OK) 200 status" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveForeignPropertyDetailsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, fullResponse))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(fullMtdJson))
      }
    }

    "return validation error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveForeignPropertyDetailsService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    protected val controller: RetrieveForeignPropertyDetailsController = new RetrieveForeignPropertyDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveForeignPropertyDetailsValidatorFactory,
      service = mockRetrieveForeignPropertyDetailsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns true

    protected def callController(): Future[Result] = controller.handleRequest(
      validNino,
      businessId.businessId,
      taxYear.asMtd,
      Some(propertyId.propertyId)
    )(fakeGetRequest)

    protected val requestData: RetrieveForeignPropertyDetailsRequestData =
      Def1_RetrieveForeignPropertyDetailsRequestData(Nino(validNino), businessId, taxYear, Some(propertyId))

  }

}
