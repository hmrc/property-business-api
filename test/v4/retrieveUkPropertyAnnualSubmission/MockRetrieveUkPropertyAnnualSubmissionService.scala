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

package v4.retrieveUkPropertyAnnualSubmission

import shared.controllers.RequestContext
import shared.services.ServiceOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import v4.retrieveUkPropertyAnnualSubmission.model.request.RetrieveUkPropertyAnnualSubmissionRequestData
import v4.retrieveUkPropertyAnnualSubmission.model.response.RetrieveUkPropertyAnnualSubmissionResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveUkPropertyAnnualSubmissionService extends TestSuite with MockFactory {

  val mockRetrieveUkPropertyAnnualSubmissionService: RetrieveUkPropertyAnnualSubmissionService = mock[RetrieveUkPropertyAnnualSubmissionService]

  object MockRetrieveUkPropertyService {

    def retrieve(requestData: RetrieveUkPropertyAnnualSubmissionRequestData)
        : CallHandler[Future[ServiceOutcome[RetrieveUkPropertyAnnualSubmissionResponse]]] = {
      (
        mockRetrieveUkPropertyAnnualSubmissionService
          .retrieveUkProperty(_: RetrieveUkPropertyAnnualSubmissionRequestData)(
            _: RequestContext,
            _: ExecutionContext
          )
        )
        .expects(requestData, *, *)
    }

  }

}
