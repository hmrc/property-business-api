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

package api.controllers.validators.resolvers

import api.models.domain.SubmissionId
import api.models.errors.SubmissionIdFormatError
import cats.data.Validated.{Invalid, Valid}
import api.support.UnitSpec

class ResolveSubmissionIdSpec extends UnitSpec {

  "ResolveSubmissionId" should {
    "return no errors" when {
      "passed a valid submission ID" in {
        val validSubmissionId = "12345678-1234-4123-9123-123456789012"
        val result            = ResolveSubmissionId(validSubmissionId)
        result shouldBe Valid(SubmissionId(validSubmissionId))
      }
    }

    "return an error" when {
      "passed an invalid submission ID" in {
        val invalidSubmissionId = "12345678-1234-4123-9123-1234567890123"
        val result              = ResolveSubmissionId(invalidSubmissionId)
        result shouldBe Invalid(List(SubmissionIdFormatError))
      }
    }
  }

}
