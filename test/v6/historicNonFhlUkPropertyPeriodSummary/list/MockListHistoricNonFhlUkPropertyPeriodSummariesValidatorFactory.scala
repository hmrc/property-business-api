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

package v6.historicNonFhlUkPropertyPeriodSummary.list

import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import v6.historicNonFhlUkPropertyPeriodSummary.list.model.request.ListHistoricNonFhlUkPropertyPeriodSummariesRequestData

trait MockListHistoricNonFhlUkPropertyPeriodSummariesValidatorFactory extends TestSuite with MockFactory {

  val mockListHistoricNonFhlUkPropertyPeriodSummariesValidatorFactory: ListHistoricNonFhlUkPropertyPeriodSummariesValidatorFactory =
    mock[ListHistoricNonFhlUkPropertyPeriodSummariesValidatorFactory]

  object MockedListHistoricNonFhlUkPropertyPeriodSummariesValidatorFactory {

    def validator(): CallHandler[Validator[ListHistoricNonFhlUkPropertyPeriodSummariesRequestData]] =
      (mockListHistoricNonFhlUkPropertyPeriodSummariesValidatorFactory.validator(_: String)).expects(*)

  }

  def willUseValidator(use: Validator[ListHistoricNonFhlUkPropertyPeriodSummariesRequestData])
      : CallHandler[Validator[ListHistoricNonFhlUkPropertyPeriodSummariesRequestData]] = {
    MockedListHistoricNonFhlUkPropertyPeriodSummariesValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(
      result: ListHistoricNonFhlUkPropertyPeriodSummariesRequestData): Validator[ListHistoricNonFhlUkPropertyPeriodSummariesRequestData] =
    new Validator[ListHistoricNonFhlUkPropertyPeriodSummariesRequestData] {
      def validate: Validated[Seq[MtdError], ListHistoricNonFhlUkPropertyPeriodSummariesRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[ListHistoricNonFhlUkPropertyPeriodSummariesRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[ListHistoricNonFhlUkPropertyPeriodSummariesRequestData] =
    new Validator[ListHistoricNonFhlUkPropertyPeriodSummariesRequestData] {
      def validate: Validated[Seq[MtdError], ListHistoricNonFhlUkPropertyPeriodSummariesRequestData] = Invalid(result)
    }

}
