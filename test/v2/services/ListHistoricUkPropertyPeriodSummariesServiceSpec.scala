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

import api.controllers.EndpointLogContext
import api.models.domain.{HistoricPropertyType, Nino}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.MockListHistoricUkPropertyPeriodSummariesConnector
import v2.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRequestData
import v2.models.response.listHistoricUkPropertyPeriodSummaries.{ListHistoricUkPropertyPeriodSummariesResponse, SubmissionPeriod}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListHistoricUkPropertyPeriodSummariesServiceSpec extends UnitSpec {

  implicit private val correlationId: String = "X-123"
  private val nino                           = Nino("AA123456A")

  "service" when {
    "service call successful" must {
      "return success result for FHL" in new Test {
        val propertyType: HistoricPropertyType = HistoricPropertyType.Fhl
        MockListHistoricUkPropertyPeriodSummariesConnector
          .listPeriodSummaries(requestData, propertyType)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.listPeriodSummaries(requestData, propertyType)) shouldBe Right(ResponseWrapper(correlationId, response))
      }

      "return success result for non-FHL" in new Test {
        val propertyType: HistoricPropertyType = HistoricPropertyType.NonFhl
        MockListHistoricUkPropertyPeriodSummariesConnector
          .listPeriodSummaries(requestData, propertyType)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.listPeriodSummaries(requestData, propertyType)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "service call unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {
            val propertyType: HistoricPropertyType = HistoricPropertyType.Fhl

            MockListHistoricUkPropertyPeriodSummariesConnector
              .listPeriodSummaries(requestData, propertyType)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.listPeriodSummaries(requestData, propertyType)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = List(
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

  trait Test extends MockListHistoricUkPropertyPeriodSummariesConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new ListHistoricUkPropertyPeriodSummariesService(
      connector = mockListHistoricUkPropertyPeriodSummariesConnector
    )

    protected val requestData: ListHistoricUkPropertyPeriodSummariesRequestData = ListHistoricUkPropertyPeriodSummariesRequestData(nino)

    protected val response: ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod] = ListHistoricUkPropertyPeriodSummariesResponse(
      Seq(SubmissionPeriod("2020-08-22", "2020-08-22")))

  }

}
