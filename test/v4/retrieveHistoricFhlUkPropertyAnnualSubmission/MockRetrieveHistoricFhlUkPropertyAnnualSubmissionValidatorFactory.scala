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

package v4.retrieveHistoricFhlUkPropertyAnnualSubmission

import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v4.retrieveHistoricFhlUkPropertyAnnualSubmission.model.request.RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData

trait MockRetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactory extends MockFactory {

  val mockRetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactory: RetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactory =
    mock[RetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactory]

  object MockedRetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData]] =
      (mockRetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData])
      : CallHandler[Validator[RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData]] = {
    MockedRetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(
      result: RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData): Validator[RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] =
    new Validator[RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] =
    new Validator[RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] = Invalid(result)
    }

}
