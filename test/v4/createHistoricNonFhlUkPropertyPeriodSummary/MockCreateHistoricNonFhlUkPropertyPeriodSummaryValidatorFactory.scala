/*
 * Copyright 2024 HM Revenue & Customs
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

package v4.createHistoricNonFhlUkPropertyPeriodSummary

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import v4.createHistoricNonFhlUkPropertyPeriodSummary.model.request.CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData

trait MockCreateHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory extends MockFactory {

  val mockCreateHistoricNonFhlUkPiePeriodSummaryValidatorFactory: CreateHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory =
    mock[CreateHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory]

  object MockedCreateHistoricNonFhlUkPiePeriodSummaryValidatorFactory {

    def validator(): CallHandler[Validator[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData]] =
      (mockCreateHistoricNonFhlUkPiePeriodSummaryValidatorFactory.validator(_: String, _: JsValue)).expects(*, *)

  }

  def willUseValidator(use: Validator[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData])
      : CallHandler[Validator[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData]] = {
    MockedCreateHistoricNonFhlUkPiePeriodSummaryValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(
      result: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData): Validator[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
    new Validator[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
    new Validator[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData] = Invalid(result)
    }

}
