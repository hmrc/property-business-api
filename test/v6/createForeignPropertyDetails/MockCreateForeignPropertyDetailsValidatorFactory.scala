/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.createForeignPropertyDetails

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.models.errors.MtdError
import v6.createForeignPropertyDetails.model.request.CreateForeignPropertyDetailsRequestData

trait MockCreateForeignPropertyDetailsValidatorFactory extends TestSuite with MockFactory {

  val mockCreateForeignPropertyDetailsValidatorFactory: CreateForeignPropertyDetailsValidatorFactory =
    mock[CreateForeignPropertyDetailsValidatorFactory]

  object MockedCreateForeignPropertyDetailsValidatorFactory {

    def validator(): CallHandler[Validator[CreateForeignPropertyDetailsRequestData]] =
      (mockCreateForeignPropertyDetailsValidatorFactory
        .validator(_: String, _: String, _: String, _: JsValue))
        .expects(*, *, *, *)

  }

  def willUseValidator(use: Validator[CreateForeignPropertyDetailsRequestData]): CallHandler[Validator[CreateForeignPropertyDetailsRequestData]] = {
    MockedCreateForeignPropertyDetailsValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: CreateForeignPropertyDetailsRequestData): Validator[CreateForeignPropertyDetailsRequestData] =
    new Validator[CreateForeignPropertyDetailsRequestData] {
      def validate: Validated[Seq[MtdError], CreateForeignPropertyDetailsRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[CreateForeignPropertyDetailsRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[CreateForeignPropertyDetailsRequestData] =
    new Validator[CreateForeignPropertyDetailsRequestData] {
      def validate: Validated[Seq[MtdError], CreateForeignPropertyDetailsRequestData] = Invalid(result)
    }

}
