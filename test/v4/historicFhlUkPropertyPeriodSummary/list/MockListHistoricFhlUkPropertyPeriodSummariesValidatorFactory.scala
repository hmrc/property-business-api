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

package v4.historicFhlUkPropertyPeriodSummary.list

import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v4.historicFhlUkPropertyPeriodSummary.list.model.request.ListHistoricFhlUkPropertyPeriodSummariesRequestData

trait MockListHistoricFhlUkPropertyPeriodSummariesValidatorFactory extends MockFactory {

  val mockListHistoricUkPropertyPeriodSummariesValidatorFactory: ListHistoricFhlUkPropertyPeriodSummariesValidatorFactory =
    mock[ListHistoricFhlUkPropertyPeriodSummariesValidatorFactory]

  object MockedListHistoricUkPropertyPeriodSummariesValidatorFactory {

    def validator(): CallHandler[Validator[ListHistoricFhlUkPropertyPeriodSummariesRequestData]] =
      (mockListHistoricUkPropertyPeriodSummariesValidatorFactory.validator(_: String)).expects(*)

  }

  def willUseValidator(
      use: Validator[ListHistoricFhlUkPropertyPeriodSummariesRequestData]
  ): CallHandler[Validator[ListHistoricFhlUkPropertyPeriodSummariesRequestData]] = {

    MockedListHistoricUkPropertyPeriodSummariesValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: ListHistoricFhlUkPropertyPeriodSummariesRequestData): Validator[ListHistoricFhlUkPropertyPeriodSummariesRequestData] =
    new Validator[ListHistoricFhlUkPropertyPeriodSummariesRequestData] {
      def validate: Validated[Seq[MtdError], ListHistoricFhlUkPropertyPeriodSummariesRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[ListHistoricFhlUkPropertyPeriodSummariesRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[ListHistoricFhlUkPropertyPeriodSummariesRequestData] =
    new Validator[ListHistoricFhlUkPropertyPeriodSummariesRequestData] {
      def validate: Validated[Seq[MtdError], ListHistoricFhlUkPropertyPeriodSummariesRequestData] = Invalid(result)
    }

}
