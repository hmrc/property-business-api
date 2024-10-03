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
import api.controllers.validators.resolvers._
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import play.api.libs.json.JsValue
import v3.controllers.validators.AmendUkPropertyAnnualSubmissionValidator.validateBusinessRules
import v3.models.request.amendUkPropertyAnnualSubmission.{AmendUkPropertyAnnualSubmissionRequestBody, AmendUkPropertyAnnualSubmissionRequestData}

import javax.inject.{Inject, Singleton}

@Singleton
class AmendUkPropertyAnnualSubmissionValidatorFactory @Inject() (appConfig: AppConfig) {

  private lazy val minimumTaxYear = appConfig.minimumTaxV2Uk

  private val resolveJson = new ResolveNonEmptyJsonObject[AmendUkPropertyAnnualSubmissionRequestBody]()

  def validator(nino: String, businessId: String, taxYear: String, body: JsValue): Validator[AmendUkPropertyAnnualSubmissionRequestData] =
    new Validator[AmendUkPropertyAnnualSubmissionRequestData] {

      def validate: Validated[Seq[MtdError], AmendUkPropertyAnnualSubmissionRequestData] =
        (
          ResolveNino(nino),
          ResolveBusinessId(businessId),
          ResolveTaxYear(minimumTaxYear, taxYear),
          resolveJson(body)
        ).mapN(AmendUkPropertyAnnualSubmissionRequestData) andThen validateBusinessRules

    }

}
