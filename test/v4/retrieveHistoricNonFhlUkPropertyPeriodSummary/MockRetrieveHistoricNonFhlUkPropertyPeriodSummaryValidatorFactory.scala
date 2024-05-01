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

package v4.retrieveHistoricNonFhlUkPropertyPeriodSummary

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v4.retrieveHistoricNonFhlUkPropertyPeriodSummary.model.request.RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData

trait MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory extends MockFactory {

  val mockRetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory: RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory =
    mock[RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory]

  object MockedRetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData]] =
      (mockRetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData])
      : CallHandler[Validator[RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData]] = {
    MockedRetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(
      result: RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData): Validator[RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
    new Validator[RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
    new Validator[RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData] = Invalid(result)
    }

}
