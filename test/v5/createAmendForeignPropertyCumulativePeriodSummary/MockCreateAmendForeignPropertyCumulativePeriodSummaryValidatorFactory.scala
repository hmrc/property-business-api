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

package v5.createAmendForeignPropertyCumulativePeriodSummary

import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import v5.createAmendForeignPropertyCumulativePeriodSummary.model.request.CreateAmendForeignPropertyCumulativePeriodSummaryRequestData

trait MockCreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactory extends MockFactory {

  val mockCreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactory: CreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactory =
    mock[CreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactory]

  object MockedCreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactory {

    def validator(): CallHandler[Validator[CreateAmendForeignPropertyCumulativePeriodSummaryRequestData]] =
      (mockCreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactory
        .validator(_: String, _: String, _: String, _: JsValue))
        .expects(*, *, *, *)

  }

  def willUseValidator(use: Validator[CreateAmendForeignPropertyCumulativePeriodSummaryRequestData])
      : CallHandler[Validator[CreateAmendForeignPropertyCumulativePeriodSummaryRequestData]] = {
    MockedCreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(
      result: CreateAmendForeignPropertyCumulativePeriodSummaryRequestData): Validator[CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
    new Validator[CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
    new Validator[CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] = Invalid(result)
    }

}
