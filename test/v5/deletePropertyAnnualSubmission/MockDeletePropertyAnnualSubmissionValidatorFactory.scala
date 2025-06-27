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

package v5.deletePropertyAnnualSubmission

import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import v5.deletePropertyAnnualSubmission.model.request.DeletePropertyAnnualSubmissionRequestData

trait MockDeletePropertyAnnualSubmissionValidatorFactory extends TestSuite with MockFactory {

  val mockDeletePropertyAnnualSubmissionValidatorFactory: DeletePropertyAnnualSubmissionValidatorFactory =
    mock[DeletePropertyAnnualSubmissionValidatorFactory]

  object MockedDeletePropertyAnnualSubmissionValidatorFactory {

    def validator(): CallHandler[Validator[DeletePropertyAnnualSubmissionRequestData]] =
      (mockDeletePropertyAnnualSubmissionValidatorFactory.validator(_: String, _: String, _: String)).expects(*, *, *)

  }

  def willUseValidator(
      use: Validator[DeletePropertyAnnualSubmissionRequestData]): CallHandler[Validator[DeletePropertyAnnualSubmissionRequestData]] = {
    MockedDeletePropertyAnnualSubmissionValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: DeletePropertyAnnualSubmissionRequestData): Validator[DeletePropertyAnnualSubmissionRequestData] =
    new Validator[DeletePropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], DeletePropertyAnnualSubmissionRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[DeletePropertyAnnualSubmissionRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[DeletePropertyAnnualSubmissionRequestData] =
    new Validator[DeletePropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], DeletePropertyAnnualSubmissionRequestData] = Invalid(result)
    }

}
