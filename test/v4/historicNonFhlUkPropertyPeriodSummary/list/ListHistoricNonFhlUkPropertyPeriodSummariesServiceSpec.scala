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

package v4.historicNonFhlUkPropertyPeriodSummary.list

import api.controllers.EndpointLogContext
import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v4.historicNonFhlUkPropertyPeriodSummary.list.def1.model.response.SubmissionPeriod
import v4.historicNonFhlUkPropertyPeriodSummary.list.model.request.{Def1_ListHistoricNonFhlUkPropertyPeriodSummariesRequestData, ListHistoricNonFhlUkPropertyPeriodSummariesRequestData}
import v4.historicNonFhlUkPropertyPeriodSummary.list.model.response.{Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse, ListHistoricNonFhlUkPropertyPeriodSummariesResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListHistoricNonFhlUkPropertyPeriodSummariesServiceSpec extends UnitSpec {

  implicit private val correlationId: String = "X-123"
  private val nino                           = Nino("AA123456A")

  "service" when {
    "downstream call is successful" should {

      "return success result" in new Test {
        MockedListHistoricNonFhlUkPropertyPeriodSummariesConnector
          .listPeriodSummaries(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.listPeriodSummaries(requestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "downstream call is unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {
            MockedListHistoricNonFhlUkPropertyPeriodSummariesConnector
              .listPeriodSummaries(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.listPeriodSummaries(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
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

  trait Test extends MockListHistoricNonFhlUkPropertyPeriodSummariesConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new ListHistoricNonFhlUkPropertyPeriodSummariesService(
      connector = mockListHistoricNonFhlUkPropertyPeriodSummariesConnector
    )

    protected val requestData: ListHistoricNonFhlUkPropertyPeriodSummariesRequestData = Def1_ListHistoricNonFhlUkPropertyPeriodSummariesRequestData(
      nino)

    protected val response: ListHistoricNonFhlUkPropertyPeriodSummariesResponse =
      Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse(
        List(SubmissionPeriod("2020-08-22", "2020-08-22"))
      )

  }

}
