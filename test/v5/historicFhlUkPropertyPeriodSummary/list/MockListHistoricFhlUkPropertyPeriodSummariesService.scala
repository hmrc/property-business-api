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

package v5.historicFhlUkPropertyPeriodSummary.list

import shared.controllers.RequestContext
import shared.services.ServiceOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v5.historicFhlUkPropertyPeriodSummary.list.def1.model.response.SubmissionPeriod
import v5.historicFhlUkPropertyPeriodSummary.list.model.request.ListHistoricFhlUkPropertyPeriodSummariesRequestData
import v5.historicFhlUkPropertyPeriodSummary.list.model.response.ListHistoricFhlUkPropertyPeriodSummariesResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockListHistoricFhlUkPropertyPeriodSummariesService extends MockFactory {

  val mockService: ListHistoricFhlUkPropertyPeriodSummariesService = mock[ListHistoricFhlUkPropertyPeriodSummariesService]

  object MockedListHistoricFhlUkPropertyPeriodSummariesService {

    def listPeriodSummaries(
        requestData: ListHistoricFhlUkPropertyPeriodSummariesRequestData
    ): CallHandler[Future[ServiceOutcome[ListHistoricFhlUkPropertyPeriodSummariesResponse[SubmissionPeriod]]]] = {
      (
        mockService
          .listPeriodSummaries(_: ListHistoricFhlUkPropertyPeriodSummariesRequestData)(
            _: RequestContext,
            _: ExecutionContext
          )
        )
        .expects(requestData, *, *)
    }

  }

}
