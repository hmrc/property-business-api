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
import v2.models.request.retrieveHistoricFhlUkPiePeriodSummary.RetrieveHistoricFhlUkPiePeriodSummaryRequestData

trait MockRetrieveHistoricFhlUkPiePeriodSummaryValidatorFactory extends MockFactory {

  val mockRetrieveHistoricFhlUkPiePeriodSummaryValidatorFactory: RetrieveHistoricFhlUkPropertyPeriodSummaryValidatorFactory =
    mock[RetrieveHistoricFhlUkPropertyPeriodSummaryValidatorFactory]

  object MockedRetrieveHistoricFhlUkPropertyPeriodSummaryValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveHistoricFhlUkPiePeriodSummaryRequestData]] =
      (mockRetrieveHistoricFhlUkPiePeriodSummaryValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(
      use: Validator[RetrieveHistoricFhlUkPiePeriodSummaryRequestData]): CallHandler[Validator[RetrieveHistoricFhlUkPiePeriodSummaryRequestData]] = {
    MockedRetrieveHistoricFhlUkPropertyPeriodSummaryValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: RetrieveHistoricFhlUkPiePeriodSummaryRequestData): Validator[RetrieveHistoricFhlUkPiePeriodSummaryRequestData] =
    new Validator[RetrieveHistoricFhlUkPiePeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveHistoricFhlUkPiePeriodSummaryRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveHistoricFhlUkPiePeriodSummaryRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveHistoricFhlUkPiePeriodSummaryRequestData] =
    new Validator[RetrieveHistoricFhlUkPiePeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveHistoricFhlUkPiePeriodSummaryRequestData] = Invalid(result)
    }

}
