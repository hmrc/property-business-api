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

package v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission

import shared.controllers.RequestContext
import shared.services.ServiceOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission.model.request.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData
import v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission.model.response.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService extends MockFactory {

  val mockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService =
    mock[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService]

  object MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService {

    def amend(requestData: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData)
        : CallHandler[Future[ServiceOutcome[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse]]] = {
      (
        mockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService
          .amend(_: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData)(
            _: RequestContext,
            _: ExecutionContext
          )
        )
        .expects(requestData, *, *)
    }

  }

}
