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

package v3.controllers.validators

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v3.models.request.listHistoricUkPropertyPeriodSummaries.ListHistoricUkPropertyPeriodSummariesRequestData

trait MockListHistoricUkPropertyPeriodSummariesValidatorFactory extends MockFactory {

  val mockListHistoricUkPropertyPeriodSummariesValidatorFactory: ListHistoricUkPropertyPeriodSummariesValidatorFactory =
    mock[ListHistoricUkPropertyPeriodSummariesValidatorFactory]

  object MockedListHistoricUkPropertyPeriodSummariesValidatorFactory {

    def validator(): CallHandler[Validator[ListHistoricUkPropertyPeriodSummariesRequestData]] =
      (mockListHistoricUkPropertyPeriodSummariesValidatorFactory.validator(_: String)).expects(*)

  }

  def willUseValidator(
      use: Validator[ListHistoricUkPropertyPeriodSummariesRequestData]): CallHandler[Validator[ListHistoricUkPropertyPeriodSummariesRequestData]] = {
    MockedListHistoricUkPropertyPeriodSummariesValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: ListHistoricUkPropertyPeriodSummariesRequestData): Validator[ListHistoricUkPropertyPeriodSummariesRequestData] =
    new Validator[ListHistoricUkPropertyPeriodSummariesRequestData] {
      def validate: Validated[Seq[MtdError], ListHistoricUkPropertyPeriodSummariesRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[ListHistoricUkPropertyPeriodSummariesRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[ListHistoricUkPropertyPeriodSummariesRequestData] =
    new Validator[ListHistoricUkPropertyPeriodSummariesRequestData] {
      def validate: Validated[Seq[MtdError], ListHistoricUkPropertyPeriodSummariesRequestData] = Invalid(result)
    }

}
