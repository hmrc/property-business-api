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

package v3.services

import api.controllers.RequestContext
import api.models.domain.HistoricPropertyType
import api.services.ServiceOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v3.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRequestData
import v3.models.response.listHistoricUkPropertyPeriodSummaries.{ListHistoricUkPropertyPeriodSummariesResponse, SubmissionPeriod}

import scala.concurrent.{ExecutionContext, Future}

trait MockListHistoricUkPropertyPeriodSummariesService extends MockFactory {

  val mockService: ListHistoricUkPropertyPeriodSummariesService = mock[ListHistoricUkPropertyPeriodSummariesService]

  object MockListHistoricUkPropertyPeriodSummariesService {

    def listPeriodSummaries(
        requestData: ListHistoricUkPropertyPeriodSummariesRequestData,
        propertyType: HistoricPropertyType): CallHandler[Future[ServiceOutcome[ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod]]]] = {
      (
        mockService
          .listPeriodSummaries(_: ListHistoricUkPropertyPeriodSummariesRequestData, _: HistoricPropertyType)(
            _: RequestContext,
            _: ExecutionContext
          )
        )
        .expects(requestData, propertyType, *, *)
    }

  }

}
