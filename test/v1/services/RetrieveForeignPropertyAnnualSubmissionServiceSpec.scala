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

package v1.services

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v1.mocks.connectors.{MockRetrieveForeignPropertyAnnualSubmissionConnector}
import v1.models.errors.{BusinessIdFormatError, DesErrorCode, DesErrors, DownstreamError, ErrorWrapper, MtdError, NinoFormatError, NotFoundError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionRequestData
import v1.models.response.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionResponse
import v1.models.response.retrieveForeignPropertyAnnualSubmission.foreignFhlEea.{ForeignFhlEeaAdjustments, ForeignFhlEeaAllowances, ForeignFhlEeaEntry}
import v1.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty.{ForeignPropertyAdjustments, ForeignPropertyAllowances, ForeignPropertyEntry}
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class RetrieveForeignPropertyAnnualSubmissionServiceSpec extends UnitSpec {


  val nino = Nino("AA123456A")
  val businessId = "XAIS12345678910"
  val taxYear = "2019-20"
  private val correlationId = "X-123"

  val response = RetrieveForeignPropertyAnnualSubmissionResponse(
    Some(ForeignFhlEeaEntry(
      Some(ForeignFhlEeaAdjustments(
        Some(100.25),
        Some(100.25),
        Some(true))),
      Some(ForeignFhlEeaAllowances(
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25))))),
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
        Some(100.25)))))))

  private val requestData = RetrieveForeignPropertyAnnualSubmissionRequestData(nino, businessId, taxYear)

  trait Test extends MockRetrieveForeignPropertyAnnualSubmissionConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveForeignPropertyAnnualSubmissionService(
      connector = mockRetrieveForeignPropertyConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockRetrieveForeignPropertyConnector.retrieveForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.retrieveForeignProperty(requestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockRetrieveForeignPropertyConnector.retrieveForeignProperty(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.retrieveForeignProperty(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
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
