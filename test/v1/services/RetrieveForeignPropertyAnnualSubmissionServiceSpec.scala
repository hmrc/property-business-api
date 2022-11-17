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

package v1.services

import support.UnitSpec
import v1.mocks.connectors.MockRetrieveForeignPropertyAnnualSubmissionConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionRequest
import v1.models.response.retrieveForeignPropertyAnnualSubmission.RetrieveForeignPropertyAnnualSubmissionResponse
import v1.models.response.retrieveForeignPropertyAnnualSubmission.foreignFhlEea._
import v1.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty._
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.models.domain.Nino

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  val nino: String                   = "AA123456A"
  val businessId: String             = "XAIS12345678910"
  val taxYear: String                = "2019-20"
  implicit val correlationId: String = "X-123"

  private val response = RetrieveForeignPropertyAnnualSubmissionResponse(
    Some(
      ForeignFhlEeaEntry(
        Some(ForeignFhlEeaAdjustments(Some(100.25), Some(100.25), Some(true))),
        Some(ForeignFhlEeaAllowances(Some(100.25), Some(100.25), Some(100.25), Some(100.25)))
      )),
    Some(
      Seq(ForeignPropertyEntry(
        "GER",
        Some(ForeignPropertyAdjustments(Some(100.25), Some(100.25))),
        Some(ForeignPropertyAllowances(Some(100.25), Some(100.25), Some(100.25), Some(100.25), Some(100.25), Some(100.25), Some(100.25)))
      )))
  )

  private val requestData = RetrieveForeignPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear)

  trait Test extends MockRetrieveForeignPropertyAnnualSubmissionConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveForeignPropertyAnnualSubmissionService(
      connector = mockRetrieveForeignPropertyConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockRetrieveForeignPropertyConnector
          .retrieveForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.retrieveForeignProperty(requestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockRetrieveForeignPropertyConnector
            .retrieveForeignProperty(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, IfsErrors.single(IfsErrorCode(ifsErrorCode))))))

          await(service.retrieveForeignProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
        "NO_DATA_FOUND"             -> NotFoundError,
        "INVALID_CORRELATIONID"     -> DownstreamError,
        "INVALID_PAYLOAD"           -> DownstreamError,
        "INVALID_TAX_YEAR"          -> DownstreamError,
        "SERVER_ERROR"              -> DownstreamError,
        "SERVICE_UNAVAILABLE"       -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
