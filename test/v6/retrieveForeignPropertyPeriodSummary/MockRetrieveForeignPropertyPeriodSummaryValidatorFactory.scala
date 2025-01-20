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

package v6.retrieveForeignPropertyPeriodSummary

import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v6.retrieveForeignPropertyPeriodSummary.model.request.RetrieveForeignPropertyPeriodSummaryRequestData

trait MockRetrieveForeignPropertyPeriodSummaryValidatorFactory extends MockFactory {

  val mockRetrieveForeignPropertyPeriodSummaryValidatorFactory: RetrieveForeignPropertyPeriodSummaryValidatorFactory =
    mock[RetrieveForeignPropertyPeriodSummaryValidatorFactory]

  object MockedRetrieveForeignPropertyPeriodSummaryValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveForeignPropertyPeriodSummaryRequestData]] =
      (mockRetrieveForeignPropertyPeriodSummaryValidatorFactory.validator(_: String, _: String, _: String, _: String)).expects(*, *, *, *)

  }

  def willUseValidator(
      use: Validator[RetrieveForeignPropertyPeriodSummaryRequestData]): CallHandler[Validator[RetrieveForeignPropertyPeriodSummaryRequestData]] = {
    MockedRetrieveForeignPropertyPeriodSummaryValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: RetrieveForeignPropertyPeriodSummaryRequestData): Validator[RetrieveForeignPropertyPeriodSummaryRequestData] =
    new Validator[RetrieveForeignPropertyPeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveForeignPropertyPeriodSummaryRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveForeignPropertyPeriodSummaryRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveForeignPropertyPeriodSummaryRequestData] =
    new Validator[RetrieveForeignPropertyPeriodSummaryRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveForeignPropertyPeriodSummaryRequestData] = Invalid(result)
    }

}
