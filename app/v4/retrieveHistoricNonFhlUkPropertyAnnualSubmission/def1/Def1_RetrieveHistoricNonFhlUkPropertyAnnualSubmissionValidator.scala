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

package v4.retrieveHistoricNonFhlUkPropertyAnnualSubmission.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveHistoricTaxYear, ResolveNino}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import v4.retrieveHistoricNonFhlUkPropertyAnnualSubmission.model.request.{
  Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData,
  RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData
}

class Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidator(nino: String, taxYear: String)(implicit appConfig: AppConfig)
    extends Validator[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] {

  private lazy val minimumTaxHistoric = appConfig.minimumTaxYearHistoric
  private lazy val maximumTaxHistoric = appConfig.maximumTaxYearHistoric

  def validate: Validated[Seq[MtdError], RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      ResolveHistoricTaxYear(minimumTaxHistoric, maximumTaxHistoric, taxYear)
    ).mapN(Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData)

}
