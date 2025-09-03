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

package v4.retrieveForeignPropertyAnnualSubmission

import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v4.retrieveForeignPropertyAnnualSubmission.def1.model.response.def1_foreignFhlEea.*
import v4.retrieveForeignPropertyAnnualSubmission.def1.model.response.def1_foreignProperty.*
import v4.retrieveForeignPropertyAnnualSubmission.model.request.*
import v4.retrieveForeignPropertyAnnualSubmission.model.response.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveForeignPropertyAnnualSubmissionService
    with MockRetrieveForeignPropertyAnnualSubmissionValidatorFactory
    with MockAuditService
    with MockIdGenerator {

  private val businessId = "XAIS12345678910"
  private val taxYear    = "2020-21"

  "RetrieveForeignPropertyAnnualSubmissionController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveForeignPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(Json.toJson(responseBody)))
      }
    }

    "return an error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(NinoFormatError)
      }

      "service errors occur" should {
        "the service returns an error" in new Test {
          willUseValidator(returningSuccess(requestData))

          MockRetrieveForeignPropertyService
            .retrieve(requestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

          runErrorTest(NotFoundError)
        }
      }
    }
  }

  trait Test extends ControllerTest {

    protected val controller = new RetrieveForeignPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveForeignPropertyAnnualSubmissionValidatorFactory,
      service = mockRetrieveForeignPropertyAnnualSubmissionService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, businessId, taxYear)(fakeRequest)

    protected val requestData: RetrieveForeignPropertyAnnualSubmissionRequestData =
      Def1_RetrieveForeignPropertyAnnualSubmissionRequestData(Nino(validNino), BusinessId(businessId), TaxYear.fromMtd(taxYear))

    protected val foreignFhlEeaEntry: Def1_Retrieve_ForeignFhlEeaEntry = Def1_Retrieve_ForeignFhlEeaEntry(
      Some(
        Def1_Retrieve_ForeignFhlEeaAdjustments(
          Some(5000.99),
          Some(5000.99),
          Some(true)
        )),
      Some(
        Def1_Retrieve_ForeignFhlEeaAllowances(
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99)
        ))
    )

    protected val foreignPropertyEntry: Def1_Retrieve_ForeignPropertyEntry = Def1_Retrieve_ForeignPropertyEntry(
      "FRA",
      Some(
        Def1_Retrieve_ForeignPropertyAdjustments(
          Some(5000.99),
          Some(5000.99)
        )),
      Some(
        Def1_Retrieve_ForeignPropertyAllowances(
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(
            List(
              Def1_Retrieve_StructuredBuildingAllowance(
                3545.12,
                Some(Def1_Retrieve_FirstYear(
                  "2020-03-29",
                  3453.34
                )),
                Def1_Retrieve_Building(
                  Some("Building Name"),
                  Some("12"),
                  "TF3 4GH"
                )
              )))
        ))
    )

    protected val responseBody: RetrieveForeignPropertyAnnualSubmissionResponse = Def1_RetrieveForeignPropertyAnnualSubmissionResponse(
      Timestamp("2020-07-07T10:59:47.544Z"),
      foreignFhlEea = Some(foreignFhlEeaEntry),
      foreignNonFhlProperty = Some(List(foreignPropertyEntry))
    )

  }

}
