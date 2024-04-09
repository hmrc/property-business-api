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

package v4.amendForeignPropertyPeriodSummary

import api.controllers.validators.Validator
import api.controllers.validators.resolvers._
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import play.api.libs.json.JsValue
import v4.amendForeignPropertyPeriodSummary.def1.AmendForeignPropertyPeriodSummaryRulesValidator.validateBusinessRules
import v4.amendForeignPropertyPeriodSummary.model.request.{AmendForeignPropertyPeriodSummaryRequestBody, AmendForeignPropertyPeriodSummaryRequestData}

import javax.inject.{Inject, Singleton}

@Singleton
class AmendForeignPropertyPeriodSummaryValidatorFactory @Inject() (appConfig: AppConfig) {

  private lazy val minTaxYear = appConfig.minimumTaxV2Foreign

  private val resolveJson = new ResolveNonEmptyJsonObject[AmendForeignPropertyPeriodSummaryRequestBody]()

  def validator(nino: String,
                businessId: String,
                taxYear: String,
                submissionId: String,
                body: JsValue): Validator[AmendForeignPropertyPeriodSummaryRequestData] =
    new Validator[AmendForeignPropertyPeriodSummaryRequestData] {

      def validate: Validated[Seq[MtdError], AmendForeignPropertyPeriodSummaryRequestData] =
        (
          ResolveNino(nino),
          ResolveBusinessId(businessId),
          ResolveTaxYear(minTaxYear, taxYear),
          ResolveSubmissionId(submissionId),
          resolveJson(body)
        ).mapN(AmendForeignPropertyPeriodSummaryRequestData) andThen validateBusinessRules

    }

}
