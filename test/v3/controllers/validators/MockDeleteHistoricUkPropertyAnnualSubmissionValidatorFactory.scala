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
import common.models.domain.HistoricPropertyType
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v3.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRequestData

trait MockDeleteHistoricUkPropertyAnnualSubmissionValidatorFactory extends MockFactory {

  val mockDeleteHistoricUkPropertyAnnualSubmissionValidatorFactory: DeleteHistoricUkPropertyAnnualSubmissionValidatorFactory =
    mock[DeleteHistoricUkPropertyAnnualSubmissionValidatorFactory]

  object MockDeleteHistoricUkPropertyAnnualSubmissionValidatorFactory {

    def validator(): CallHandler[Validator[DeleteHistoricUkPropertyAnnualSubmissionRequestData]] =
      (mockDeleteHistoricUkPropertyAnnualSubmissionValidatorFactory.validator(_: String, _: String, _: HistoricPropertyType)).expects(*, *, *)

  }

  def willUseValidator(use: Validator[DeleteHistoricUkPropertyAnnualSubmissionRequestData])
      : CallHandler[Validator[DeleteHistoricUkPropertyAnnualSubmissionRequestData]] = {
    MockDeleteHistoricUkPropertyAnnualSubmissionValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: DeleteHistoricUkPropertyAnnualSubmissionRequestData): Validator[DeleteHistoricUkPropertyAnnualSubmissionRequestData] =
    new Validator[DeleteHistoricUkPropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], DeleteHistoricUkPropertyAnnualSubmissionRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[DeleteHistoricUkPropertyAnnualSubmissionRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[DeleteHistoricUkPropertyAnnualSubmissionRequestData] =
    new Validator[DeleteHistoricUkPropertyAnnualSubmissionRequestData] {
      def validate: Validated[Seq[MtdError], DeleteHistoricUkPropertyAnnualSubmissionRequestData] = Invalid(result)
    }

}
