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

import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import v4.retrieveUkPropertyAnnualSubmission.model.request.RetrieveUkPropertyAnnualSubmissionRequestData

trait MockRetrieveUkPropertyAnnualSubmissionValidatorFactory extends TestSuite with MockFactory {

  val mockRetrieveUkPropertyAnnualSubmissionValidatorFactory: RetrieveUkPropertyAnnualSubmissionValidatorFactory =
    mock[RetrieveUkPropertyAnnualSubmissionValidatorFactory]

  object MockedRetrieveUkPropertyAnnualSubmissionValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveUkPropertyAnnualSubmissionRequestData]] =
      (mockRetrieveUkPropertyAnnualSubmissionValidatorFactory.validator(_: String, _: String, _: String)).expects(*, *, *)

  }

  def willUseValidator(
      use: Validator[RetrieveUkPropertyAnnualSubmissionRequestData]): CallHandler[Validator[RetrieveUkPropertyAnnualSubmissionRequestData]] = {
    MockedRetrieveUkPropertyAnnualSubmissionValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: RetrieveUkPropertyAnnualSubmissionRequestData): Validator[RetrieveUkPropertyAnnualSubmissionRequestData] =
    new Validator[RetrieveUkPropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveUkPropertyAnnualSubmissionRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveUkPropertyAnnualSubmissionRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveUkPropertyAnnualSubmissionRequestData] =
    new Validator[RetrieveUkPropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveUkPropertyAnnualSubmissionRequestData] = Invalid(result)
    }

}
