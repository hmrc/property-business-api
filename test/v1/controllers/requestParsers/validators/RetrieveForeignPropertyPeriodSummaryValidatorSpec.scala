/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import support.UnitSpec
import v1.models.errors.{ BusinessIdFormatError, NinoFormatError, SubmissionIdFormatError }
import v1.models.request.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRawData

class RetrieveForeignPropertyPeriodSummaryValidatorSpec extends UnitSpec {

  private val validNino         = "AA123456A"
  private val validBusinessId   = "XAIS12345678901"
  private val validSubmissionId = "12345678-1234-4123-9123-123456789012"

  private val validator = new RetrieveForeignPropertyPeriodSummaryValidator

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(RetrieveForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, validSubmissionId)) shouldBe Nil
      }
    }
    "return a path parameter format error" when {
      "an invalid nino is supplied" in {
        validator.validate(RetrieveForeignPropertyPeriodSummaryRawData("Walrus", validBusinessId, validSubmissionId)) shouldBe List(NinoFormatError)
      }
      "an invalid businessId is supplied" in {
        validator.validate(RetrieveForeignPropertyPeriodSummaryRawData(validNino, "Beans", validSubmissionId)) shouldBe List(BusinessIdFormatError)
      }
      "an invalid submissionId is supplied" in {
        validator.validate(RetrieveForeignPropertyPeriodSummaryRawData(validNino, validBusinessId, "ABCDEFGHIJKLMNOPQRSTUVWXYZ")) shouldBe List(
          SubmissionIdFormatError)
      }
      "multiple format errors are made" in {
        validator.validate(RetrieveForeignPropertyPeriodSummaryRawData("Walrus", "Beans", "ABCDEFGHIJKLMNOPQRSTUVWXYZ")) shouldBe List(
          NinoFormatError,
          BusinessIdFormatError,
          SubmissionIdFormatError)
      }
    }
  }
}
