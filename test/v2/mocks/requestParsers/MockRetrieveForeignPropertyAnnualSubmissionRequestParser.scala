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

package v2.mocks.requestParsers

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v2.controllers.requestParsers.RetrieveForeignPropertyAnnualSubmissionRequestParser
import api.models.errors.ErrorWrapper
import v2.models.request.retrieveForeignPropertyAnnualSubmission._

trait MockRetrieveForeignPropertyAnnualSubmissionRequestParser extends MockFactory {

  val mockRetrieveForeignPropertyAnnualSubmissionRequestParser: RetrieveForeignPropertyAnnualSubmissionRequestParser =
    mock[RetrieveForeignPropertyAnnualSubmissionRequestParser]

  object MockRetrieveForeignPropertyRequestParser {

    def parse(
        data: RetrieveForeignPropertyAnnualSubmissionRawData): CallHandler[Either[ErrorWrapper, RetrieveForeignPropertyAnnualSubmissionRequest]] = {
      (mockRetrieveForeignPropertyAnnualSubmissionRequestParser
        .parseRequest(_: RetrieveForeignPropertyAnnualSubmissionRawData)(_: String))
        .expects(data, *)
    }
  }
}
