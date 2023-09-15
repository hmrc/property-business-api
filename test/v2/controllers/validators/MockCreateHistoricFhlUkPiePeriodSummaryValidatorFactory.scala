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
import play.api.libs.json.JsValue
import v2.models.request.createHistoricFhlUkPiePeriodSummary.CreateHistoricFhlUkPiePeriodSummaryRequestData

trait MockCreateHistoricFhlUkPiePeriodSummaryValidatorFactory extends MockFactory {

  val mockCreateHistoricFhlUkPiePeriodSummaryValidatorFactory: CreateHistoricFhlUkPiePeriodSummaryValidatorFactory =
    mock[CreateHistoricFhlUkPiePeriodSummaryValidatorFactory]

  object MockedCreateHistoricFhlUkPiePeriodSummaryValidatorFactory {

    def validator(): CallHandler[Validator[CreateHistoricFhlUkPiePeriodSummaryRequestData]] =
      (mockCreateHistoricFhlUkPiePeriodSummaryValidatorFactory.validator(_: String, _: JsValue)).expects(*, *)

  }

  def willUseValidator(
      use: Validator[CreateHistoricFhlUkPiePeriodSummaryRequestData]): CallHandler[Validator[CreateHistoricFhlUkPiePeriodSummaryRequestData]] = {
    MockedCreateHistoricFhlUkPiePeriodSummaryValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: CreateHistoricFhlUkPiePeriodSummaryRequestData): Validator[CreateHistoricFhlUkPiePeriodSummaryRequestData] =
    new Validator[CreateHistoricFhlUkPiePeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], CreateHistoricFhlUkPiePeriodSummaryRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[CreateHistoricFhlUkPiePeriodSummaryRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[CreateHistoricFhlUkPiePeriodSummaryRequestData] =
    new Validator[CreateHistoricFhlUkPiePeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], CreateHistoricFhlUkPiePeriodSummaryRequestData] = Invalid(result)
    }

}
