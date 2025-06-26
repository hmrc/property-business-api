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

package v4.historicFhlUkPropertyPeriodSummary.amend

import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.libs.json.JsValue
import v4.historicFhlUkPropertyPeriodSummary.amend.request.AmendHistoricFhlUkPropertyPeriodSummaryRequestData

trait MockAmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory extends TestSuite with MockFactory {

  val mockAmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory: AmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory =
    mock[AmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory]

  object MockedAmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory {

    def validator(): CallHandler[Validator[AmendHistoricFhlUkPropertyPeriodSummaryRequestData]] =
      (mockAmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory.validator(_: String, _: String, _: JsValue)).expects(*, *, *)

  }

  def willUseValidator(use: Validator[AmendHistoricFhlUkPropertyPeriodSummaryRequestData])
      : CallHandler[Validator[AmendHistoricFhlUkPropertyPeriodSummaryRequestData]] = {
    MockedAmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: AmendHistoricFhlUkPropertyPeriodSummaryRequestData): Validator[AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
    new Validator[AmendHistoricFhlUkPropertyPeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], AmendHistoricFhlUkPropertyPeriodSummaryRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[AmendHistoricFhlUkPropertyPeriodSummaryRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
    new Validator[AmendHistoricFhlUkPropertyPeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], AmendHistoricFhlUkPropertyPeriodSummaryRequestData] = Invalid(result)
    }

}
