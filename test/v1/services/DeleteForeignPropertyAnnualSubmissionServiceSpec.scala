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

package v1.services

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockDeleteForeignPropertyAnnualSubmissionConnector
import v1.models.errors.{BusinessIdFormatError, DesErrorCode, DesErrors, DownstreamError, ErrorWrapper, MtdError, NinoFormatError, NotFoundError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.deleteForeignPropertyAnnualSubmission.DeleteForeignPropertyAnnualSubmissionRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteForeignPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  val nino = Nino("AA123456A")
  val businessId = "XAIS12345678910"
  val taxYear = "2021-22"
  implicit val correlationId = "X-123"

  private val requestData = DeleteForeignPropertyAnnualSubmissionRequest(nino, businessId, taxYear)

  trait Test extends MockDeleteForeignPropertyAnnualSubmissionConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new DeleteForeignPropertyAnnualSubmissionService(
      connector = mockDeleteForeignPropertyAnnualSubmissionConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockDeleteForeignPropertyAnnualSubmissionConnector.deleteForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.deleteForeignPropertyAnnualSubmission(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockDeleteForeignPropertyAnnualSubmissionConnector.deleteForeignProperty(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.deleteForeignPropertyAnnualSubmission(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_INCOME_SOURCE_ID" -> BusinessIdFormatError,
        "NOT_FOUND" -> NotFoundError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}

