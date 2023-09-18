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

package v2.controllers.validators

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v2.models.request.retrieveHistoricNonFhlUkPiePeriodSummary.RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData

trait MockRetrieveHistoricNonFhlUkPiePeriodSummaryValidatorFactory extends MockFactory {

  val mockRetrieveHistoricNonFhlUkPiePeriodSummaryValidatorFactory: RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory =
    mock[RetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory]

  object MockedRetrieveHistoricNonFhlUkPiePeriodSummaryValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData]] =
      (mockRetrieveHistoricNonFhlUkPiePeriodSummaryValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData])
      : CallHandler[Validator[RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData]] = {
    MockedRetrieveHistoricNonFhlUkPiePeriodSummaryValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData): Validator[RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData] =
    new Validator[RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData] =
    new Validator[RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveHistoricNonFhlUkPiePeriodSummaryRequestData] = Invalid(result)
    }

}
