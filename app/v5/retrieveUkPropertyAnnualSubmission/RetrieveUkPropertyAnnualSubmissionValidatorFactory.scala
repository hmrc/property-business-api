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

package v5.retrieveUkPropertyAnnualSubmission

import api.controllers.validators.Validator
import api.models.domain.TaxYear
import config.AppConfig
import v5.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionValidatorFactory.def2TaxYearStart
import v5.retrieveUkPropertyAnnualSubmission.def1.model.Def1_RetrieveUkPropertyAnnualSubmissionValidator
import v5.retrieveUkPropertyAnnualSubmission.def2.model.Def2_RetrieveUkPropertyAnnualSubmissionValidator
import v5.retrieveUkPropertyAnnualSubmission.model.request.RetrieveUkPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}
import scala.math.Ordered.orderingToOrdered

@Singleton
class RetrieveUkPropertyAnnualSubmissionValidatorFactory @Inject() (appConfig: AppConfig) {

  def validator(nino: String, businessId: String, taxYear: String): Validator[RetrieveUkPropertyAnnualSubmissionRequestData] =
    TaxYear.maybeFromMtd(taxYear) match {
      case Some(parsedTY) if parsedTY >= def2TaxYearStart =>
        new Def2_RetrieveUkPropertyAnnualSubmissionValidator(nino, businessId, taxYear)

      case _ =>
        new Def1_RetrieveUkPropertyAnnualSubmissionValidator(nino, businessId, taxYear)(appConfig)

    }
}

object  RetrieveUkPropertyAnnualSubmissionValidatorFactory {
  private val def2TaxYearStart = TaxYear.fromMtd("2024-25")
}
