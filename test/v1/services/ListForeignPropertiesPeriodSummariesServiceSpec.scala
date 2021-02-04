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
import v1.mocks.connectors.MockListForeignPropertiesPeriodSummariesConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listForeignPropertiesPeriodSummaries.ListForeignPropertiesPeriodSummariesRequest
import v1.models.response.listForeignPropertiesPeriodSummaries.{ListForeignPropertiesPeriodSummariesResponse, SubmissionPeriod}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListForeignPropertiesPeriodSummariesServiceSpec extends UnitSpec {


  val nino = Nino("AA123456A")
  val businessId = "XAIS12345678910"
  val fromDate = "2020-06-01"
  val toDate = "2020-08-31"
  implicit val correlationId = "X-123"

  val request = ListForeignPropertiesPeriodSummariesRequest(nino, businessId, fromDate, toDate)

  val response = ListForeignPropertiesPeriodSummariesResponse(Seq(
    SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22"),
    SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3d", "2020-08-22", "2020-08-22")
  ))

  trait Test extends MockListForeignPropertiesPeriodSummariesConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new ListForeignPropertiesPeriodSummariesService(
      connector = mockListForeignPropertiesConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockListForeignPropertiesConnector.listForeignProperties(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.listForeignProperties(request)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockListForeignPropertiesConnector.listForeignProperties(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.listForeignProperties(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "FORMAT_BUSINESS_ID" -> BusinessIdFormatError,
        "NOT_FOUND" -> NotFoundError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
