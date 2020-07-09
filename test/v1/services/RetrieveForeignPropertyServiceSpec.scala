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
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockRetrieveForeignPropertyConnector
import v1.models.errors.{BusinessIdFormatError, DesErrorCode, DesErrors, DownstreamError, ErrorWrapper, MtdError, NinoFormatError, NotFoundError, SubmissionIdFormatError, SubmissionIdNotFoundError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveForeignProperty.RetrieveForeignPropertyRequestData
import v1.models.response.retrieveForeignProperty.RetrieveForeignPropertyResponse
import v1.models.response.retrieveForeignProperty.foreignFhlEea.{ForeignFhlEea, ForeignFhlEeaExpenditure, ForeignFhlEeaIncome}
import v1.models.response.retrieveForeignProperty.foreignProperty.{ForeignProperty, ForeignPropertyExpenditure, ForeignPropertyIncome, ForeignPropertyRentIncome}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyServiceSpec extends UnitSpec {

  val nino = Nino("AA123456A")
  val businessId = "XAIS12345678910"
  val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val correlationId = "X-123"

  val response = RetrieveForeignPropertyResponse(
    "2020-01-01",
    "2020-01-31",
    Some(ForeignFhlEea(
      ForeignFhlEeaIncome(5000.99, Some(5000.99)),
      Some(ForeignFhlEeaExpenditure(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        None
      ))
    )),
    Some(Seq(ForeignProperty("FRA",
      ForeignPropertyIncome(
        ForeignPropertyRentIncome(5000.99, Some(5000.99)),
        false,
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99)
      ),
      Some(ForeignPropertyExpenditure(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        None
      ))))
    ))

  private val requestData = RetrieveForeignPropertyRequestData(nino, businessId, submissionId)

  trait Test extends MockRetrieveForeignPropertyConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveForeignPropertyService(
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
        "FORMAT_BUSINESS_ID" -> BusinessIdFormatError,
        "FORMAT_SUBMISSION_ID" -> SubmissionIdFormatError,
        "NOT_FOUND" -> NotFoundError,
        "SUBMISSION_ID_NOT_FOUND" -> SubmissionIdNotFoundError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
