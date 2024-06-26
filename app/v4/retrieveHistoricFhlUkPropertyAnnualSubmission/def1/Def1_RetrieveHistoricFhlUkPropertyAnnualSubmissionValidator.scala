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

package v4.retrieveHistoricFhlUkPropertyAnnualSubmission.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveHistoricTaxYear, ResolveNino}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import v4.retrieveHistoricFhlUkPropertyAnnualSubmission.model.request._

class Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionValidator(nino: String, taxYear: String, appConfig: AppConfig)
    extends Validator[RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] {

  private lazy val minimumTaxYear = appConfig.minimumTaxYearHistoric
  private lazy val maximumTaxYear = appConfig.maximumTaxYearHistoric

  def validate: Validated[Seq[MtdError], RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      ResolveHistoricTaxYear(minimumTaxYear, maximumTaxYear, taxYear)
    ).mapN(Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData)

}
