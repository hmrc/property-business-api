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

package v4.deleteHistoricNonFhlUkPropertyAnnualSubmission.def1

import cats.data.Validated
import cats.implicits.catsSyntaxTuple2Semigroupal
import common.controllers.validators.resolvers.ResolveHistoricTaxYear
import config.AppConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.ResolveNino
import shared.models.errors.MtdError
import v4.deleteHistoricNonFhlUkPropertyAnnualSubmission.model.request.{
  Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData,
  DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData
}

class Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionValidator(nino: String, taxYear: String)(implicit appConfig: AppConfig)
    extends Validator[DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData] {

  private lazy val minimumTaxHistoric = appConfig.minimumTaxYearHistoric
  private lazy val maximumTaxHistoric = appConfig.maximumTaxYearHistoric

  def validate: Validated[Seq[MtdError], DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      ResolveHistoricTaxYear(minimumTaxHistoric, maximumTaxHistoric, taxYear)
    ).mapN(Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData)

}
