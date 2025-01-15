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

package v4.retrieveUkPropertyPeriodSummary

import shared.controllers.validators.Validator
import shared.models.domain.TaxYear
import v4.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryValidatorFactory.def2TaxYearStart
import v4.retrieveUkPropertyPeriodSummary.def1.Def1_RetrieveUkPropertyPeriodSummaryValidator
import v4.retrieveUkPropertyPeriodSummary.def2.Def2_RetrieveUkPropertyPeriodSummaryValidator
import v4.retrieveUkPropertyPeriodSummary.model.request.RetrieveUkPropertyPeriodSummaryRequestData

import javax.inject.{Inject, Singleton}
import scala.math.Ordering.Implicits.infixOrderingOps

@Singleton
class RetrieveUkPropertyPeriodSummaryValidatorFactory @Inject() {

  def validator(nino: String, businessId: String, taxYear: String, submissionId: String): Validator[RetrieveUkPropertyPeriodSummaryRequestData] = {
    TaxYear.maybeFromMtd(taxYear) match {
      case Some(parsedTY) if parsedTY >= def2TaxYearStart =>
        new Def2_RetrieveUkPropertyPeriodSummaryValidator(nino, businessId, taxYear, submissionId)

      case _ =>
        new Def1_RetrieveUkPropertyPeriodSummaryValidator(nino, businessId, taxYear, submissionId)
    }
  }

}

object RetrieveUkPropertyPeriodSummaryValidatorFactory {

  private val def2TaxYearStart = TaxYear.fromMtd("2024-25")

}
