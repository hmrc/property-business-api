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

package v4.deletePropertyAnnualSubmission.def1

import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveTaxYear}
import shared.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import v4.deletePropertyAnnualSubmission.model.request.{Def1_DeletePropertyAnnualSubmissionRequestData, DeletePropertyAnnualSubmissionRequestData}

import javax.inject.Inject

class Def1_DeletePropertyAnnualSubmissionValidator @Inject() (nino: String, businessId: String, taxYear: String)(appConfig: AppConfig)
    extends Validator[DeletePropertyAnnualSubmissionRequestData] {

  private lazy val minimumTaxYear = appConfig.minimumTaxV2Foreign

  def validate: Validated[Seq[MtdError], DeletePropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveTaxYear(minimumTaxYear, taxYear)
    ).mapN(Def1_DeletePropertyAnnualSubmissionRequestData)

}
