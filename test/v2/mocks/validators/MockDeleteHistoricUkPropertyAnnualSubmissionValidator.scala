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

package v2.mocks.validators

import api.models.errors.MtdError
import org.scalamock.handlers.CallHandler1
import org.scalamock.scalatest.MockFactory
import v2.controllers.requestParsers.validators.DeleteHistoricUkPropertyAnnualSubmissionValidator
import v2.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRawData

class MockDeleteHistoricUkPropertyAnnualSubmissionValidator extends MockFactory {

  val mockValidator: DeleteHistoricUkPropertyAnnualSubmissionValidator = mock[DeleteHistoricUkPropertyAnnualSubmissionValidator]

  object MockDeleteHistoricUkPropertyAnnualSubmissionValidator {

    def validate(
        data: DeleteHistoricUkPropertyAnnualSubmissionRawData): CallHandler1[DeleteHistoricUkPropertyAnnualSubmissionRawData, List[MtdError]] = {
      (mockValidator
        .validate(_: DeleteHistoricUkPropertyAnnualSubmissionRawData))
        .expects(data)
    }

  }

}