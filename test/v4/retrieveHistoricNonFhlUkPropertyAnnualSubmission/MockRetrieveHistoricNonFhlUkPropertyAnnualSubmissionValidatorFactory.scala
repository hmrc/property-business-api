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

package v4.retrieveHistoricNonFhlUkPropertyAnnualSubmission

import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import v4.retrieveHistoricNonFhlUkPropertyAnnualSubmission.model.request.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData

trait MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory extends TestSuite with MockFactory {

  val mockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory =
    mock[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory]

  object MockedRetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData]] =
      (mockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData])
      : CallHandler[Validator[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData]] = {
    MockedRetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(
      result: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData): Validator[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
    new Validator[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
    new Validator[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] = Invalid(result)
    }

}
