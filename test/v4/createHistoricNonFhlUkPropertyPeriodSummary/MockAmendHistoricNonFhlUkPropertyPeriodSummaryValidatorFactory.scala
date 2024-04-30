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

package v4.createHistoricNonFhlUkPropertyPeriodSummary

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import v4.amendHistoricNonFhlUkPropertyPeriodSummary.AmendHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory
import v4.amendHistoricNonFhlUkPropertyPeriodSummary.model.request.AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData

trait MockAmendHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory extends MockFactory {

  val mockAmendHistoricNonFhlUkPeriodSummaryValidatorFactory: AmendHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory =
    mock[AmendHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory]

  object MockedAmendHistoricNonFhlUkPeriodSummaryValidatorFactory {

    def validator(): CallHandler[Validator[AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData]] =
      (mockAmendHistoricNonFhlUkPeriodSummaryValidatorFactory.validator(_: String, _: String, _: JsValue)).expects(*, *, *)

  }

  def willUseValidator(
      use: Validator[AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData]): CallHandler[Validator[AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData]] = {
    MockedAmendHistoricNonFhlUkPeriodSummaryValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData): Validator[AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
    new Validator[AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
    new Validator[AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] = Invalid(result)
    }

}
