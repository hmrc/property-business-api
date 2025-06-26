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

package v4.deleteHistoricFhlUkPropertyAnnualSubmission

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import common.models.domain.HistoricPropertyType
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v4.deleteHistoricFhlUkPropertyAnnualSubmission.model.request.DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData

trait MockDeleteHistoricFhlUkPropertyAnnualSubmissionValidatorFactory extends TestSuite with MockFactory {

  val mockDeleteHistoricUkPropertyAnnualSubmissionValidatorFactory: DeleteHistoricFhlUkPropertyAnnualSubmissionValidatorFactory =
    mock[DeleteHistoricFhlUkPropertyAnnualSubmissionValidatorFactory]

  object MockDeleteHistoricUkPropertyAnnualSubmissionValidatorFactory {

    def validator(): CallHandler[Validator[DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData]] =
      (mockDeleteHistoricUkPropertyAnnualSubmissionValidatorFactory.validator(_: String, _: String, _: HistoricPropertyType)).expects(*, *, *)

  }

  def willUseValidator(use: Validator[DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData])
      : CallHandler[Validator[DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData]] = {
    MockDeleteHistoricUkPropertyAnnualSubmissionValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(
      result: DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData): Validator[DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] =
    new Validator[DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] =
    new Validator[DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] = Invalid(result)
    }

}
