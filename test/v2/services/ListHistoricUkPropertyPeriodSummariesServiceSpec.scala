/*
 * Copyright 2023 HM Revenue & Customs
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

package v2.services

import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import api.controllers.EndpointLogContext
import v2.mocks.connectors.MockListHistoricUkPropertyPeriodSummariesConnector
import v2.models.domain.HistoricPropertyType
import api.models.errors._
import api.models.ResponseWrapper
import api.models.domain.Nino
import v2.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRequest
import v2.models.response.listHistoricUkPropertyPeriodSummaries.{ ListHistoricUkPropertyPeriodSummariesResponse, SubmissionPeriod }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListHistoricUkPropertyPeriodSummariesServiceSpec extends UnitSpec {

  val nino: String                   = "AA123456A"
  val businessId: String             = "XAIS12345678910"
  val taxYear: String                = "2021-22"
  implicit val correlationId: String = "X-123"

  private val request = ListHistoricUkPropertyPeriodSummariesRequest(Nino(nino))

  private val response = ListHistoricUkPropertyPeriodSummariesResponse(
    Seq(
      SubmissionPeriod("2020-08-22", "2020-08-22")
    ))

  trait Test extends MockListHistoricUkPropertyPeriodSummariesConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new ListHistoricUkPropertyPeriodSummariesService(
      connector = mockListHistoricUkPropertyPeriodSummariesConnector
    )
  }

  "service" when {
    "service call successful" must {
      "return success result for FHL" in new Test {
        val propertyType: HistoricPropertyType = HistoricPropertyType.Fhl
        MockListHistoricUkPropertyPeriodSummariesConnector
          .listPeriodSummaries(request, propertyType)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.listPeriodSummaries(request, propertyType)) shouldBe Right(ResponseWrapper(correlationId, response))
      }

      "return success result for non-FHL" in new Test {
        val propertyType: HistoricPropertyType = HistoricPropertyType.NonFhl
        MockListHistoricUkPropertyPeriodSummariesConnector
          .listPeriodSummaries(request, propertyType)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.listPeriodSummaries(request, propertyType)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "service call unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(ifsErrorCode: String, error: MtdError): Unit =
          s"a $ifsErrorCode error is returned from the service" in new Test {
            val propertyType: HistoricPropertyType = HistoricPropertyType.Fhl

            MockListHistoricUkPropertyPeriodSummariesConnector
              .listPeriodSummaries(request, propertyType)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

            await(service.listPeriodSummaries(request, propertyType)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          "INVALID_NINO"           -> NinoFormatError,
          "INVALID_CORRELATIONID"  -> InternalError,
          "TAX_YEAR_NOT_SUPPORTED" -> InternalError,
          "INVALID_TYPE"           -> InternalError,
          "SERVER_ERROR"           -> InternalError,
          "SERVICE_UNAVAILABLE"    -> InternalError
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
