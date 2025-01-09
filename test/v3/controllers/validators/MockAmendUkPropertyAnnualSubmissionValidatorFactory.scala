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

package v3.controllers.validators

import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import v3.models.request.amendUkPropertyAnnualSubmission.AmendUkPropertyAnnualSubmissionRequestData

trait MockAmendUkPropertyAnnualSubmissionValidatorFactory extends MockFactory {

  val mockAmendUkPropertyAnnualSubmissionValidatorFactory: AmendUkPropertyAnnualSubmissionValidatorFactory =
    mock[AmendUkPropertyAnnualSubmissionValidatorFactory]

  object MockedAmendUkPropertyAnnualSubmissionValidatorFactory {

    def validator(): CallHandler[Validator[AmendUkPropertyAnnualSubmissionRequestData]] =
      (mockAmendUkPropertyAnnualSubmissionValidatorFactory.validator(_: String, _: String, _: String, _: JsValue)).expects(*, *, *, *)

  }

  def willUseValidator(
      use: Validator[AmendUkPropertyAnnualSubmissionRequestData]): CallHandler[Validator[AmendUkPropertyAnnualSubmissionRequestData]] = {
    MockedAmendUkPropertyAnnualSubmissionValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: AmendUkPropertyAnnualSubmissionRequestData): Validator[AmendUkPropertyAnnualSubmissionRequestData] =
    new Validator[AmendUkPropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], AmendUkPropertyAnnualSubmissionRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[AmendUkPropertyAnnualSubmissionRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[AmendUkPropertyAnnualSubmissionRequestData] =
    new Validator[AmendUkPropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], AmendUkPropertyAnnualSubmissionRequestData] = Invalid(result)
    }

}
