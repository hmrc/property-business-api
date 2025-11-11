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

package v6.updateForeignPropertyDetails

import common.models.domain.PropertyId
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import v6.updateForeignPropertyDetails.def1.model.Def1_UpdateForeignPropertyDetailsFixtures.def1_UpdateForeignPropertyDetailsModel
import v6.updateForeignPropertyDetails.def1.model.request.Def1_UpdateForeignPropertyDetailsRequestData
import v6.updateForeignPropertyDetails.model.request.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpdateForeignPropertyDetailsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockUpdateForeignPropertyDetailsService
    with MockUpdateForeignPropertyDetailsValidatorFactory {

  private val propertyId                         = PropertyId("8e8b8450-dc1b-4360-8109-7067337b42cb")
  private val taxYear                            = TaxYear.fromMtd("2026-27")
  def fakePutRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  "UpdateForeignPropertyDetailsController" should {
    "return successful 204 status" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedUpdateForeignPropertyDetailsService
          .update(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTest(expectedStatus = NO_CONTENT)
      }
    }

    "return validation error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedUpdateForeignPropertyDetailsService
          .update(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    protected val controller: UpdateForeignPropertyDetailsController = new UpdateForeignPropertyDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockUpdateForeignPropertyDetailsValidatorFactory,
      service = mockUpdateForeignPropertyDetailsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns true

    val requestBody: JsObject = Json.obj(
      "propertyName" -> "Bob & Bobby Co",
      "endDate"      -> "2026-08-24",
      "endReason"    -> "no-longer-renting-property-out"
    )

    protected def callController(): Future[Result] = controller.handleRequest(
      validNino,
      propertyId.propertyId,
      taxYear.asMtd
    )(fakePutRequest(requestBody))

    protected val requestData: UpdateForeignPropertyDetailsRequestData =
      Def1_UpdateForeignPropertyDetailsRequestData(Nino(validNino), propertyId, taxYear, def1_UpdateForeignPropertyDetailsModel)

  }

}
