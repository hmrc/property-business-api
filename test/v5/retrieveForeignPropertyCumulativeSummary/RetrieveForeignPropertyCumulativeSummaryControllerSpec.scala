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

package v5.retrieveForeignPropertyCumulativeSummary

import play.api.Configuration
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v5.retrieveForeignPropertyCumulativeSummary.def1.model.Def1_RetrieveForeignPropertyCumulativeSummaryFixture
import v5.retrieveForeignPropertyCumulativeSummary.def1.model.request.Def1_RetrieveForeignPropertyCumulativeSummaryRequestData
import v5.retrieveForeignPropertyCumulativeSummary.model.request._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyCumulativeSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveForeignPropertyCumulativeSummaryService
    with MockRetrieveForeignPropertyCumulativeSummaryValidatorFactory
    with Def1_RetrieveForeignPropertyCumulativeSummaryFixture {

  private val businessId = BusinessId("XAIS12345678910")
  private val taxYear    = TaxYear.fromMtd("2022-23")

  "RetrieveForeignPropertyCumulativeSummaryController" should {
    "return (OK) 200 status" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveForeignPropertyService
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

        MockRetrieveForeignPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    protected val controller = new RetrieveForeignPropertyCumulativeSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveForeignPropertyCumulativeSummaryValidatorFactory,
      service = mockRetrieveForeignPropertyService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, businessId.businessId, taxYear.asMtd)(fakeGetRequest)

    protected val requestData: RetrieveForeignPropertyCumulativeSummaryRequestData =
      Def1_RetrieveForeignPropertyCumulativeSummaryRequestData(Nino(validNino), businessId, taxYear)

  }

}
