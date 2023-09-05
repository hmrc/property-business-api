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

package v2.controllers.validators

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveSubmissionId, ResolveTaxYear}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import v2.models.request.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveForeignPropertyPeriodSummaryValidatorFactory @Inject() (appConfig: AppConfig) {

  def validator(nino: String, businessId: String, taxYear: String, submissionId: String): Validator[RetrieveForeignPropertyPeriodSummaryRequestData] =
    new Validator[RetrieveForeignPropertyPeriodSummaryRequestData] {

      def validate: Validated[Seq[MtdError], RetrieveForeignPropertyPeriodSummaryRequestData] =
        (
          ResolveNino(nino),
          ResolveBusinessId(businessId),
          ResolveTaxYear(appConfig.minimumTaxV2Foreign, taxYear, None, None),
          ResolveSubmissionId(submissionId)
        ).mapN(RetrieveForeignPropertyPeriodSummaryRequestData)

    }

}
