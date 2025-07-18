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

package v4.propertyPeriodSummary.list

import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import v4.propertyPeriodSummary.list.model.request.ListPropertyPeriodSummariesRequestData

trait MockListPropertyPeriodSummariesValidatorFactory extends TestSuite with MockFactory {

  val mockListPropertyPeriodSummariesValidatorFactory: ListPropertyPeriodSummariesValidatorFactory =
    mock[ListPropertyPeriodSummariesValidatorFactory]

  object MockedListPropertyPeriodSummariesValidatorFactory {

    def validator(): CallHandler[Validator[ListPropertyPeriodSummariesRequestData]] =
      (mockListPropertyPeriodSummariesValidatorFactory.validator(_: String, _: String, _: String)).expects(*, *, *)

  }

  def willUseValidator(use: Validator[ListPropertyPeriodSummariesRequestData]): CallHandler[Validator[ListPropertyPeriodSummariesRequestData]] = {
    MockedListPropertyPeriodSummariesValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: ListPropertyPeriodSummariesRequestData): Validator[ListPropertyPeriodSummariesRequestData] =
    new Validator[ListPropertyPeriodSummariesRequestData] {
      def validate: Validated[Seq[MtdError], ListPropertyPeriodSummariesRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[ListPropertyPeriodSummariesRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[ListPropertyPeriodSummariesRequestData] =
    new Validator[ListPropertyPeriodSummariesRequestData] {
      def validate: Validated[Seq[MtdError], ListPropertyPeriodSummariesRequestData] = Invalid(result)
    }

}
