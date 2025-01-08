/*
 * Copyright 2024 HM Revenue & Customs
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

package v4.amendUkPropertyPeriodSummary

import shared.controllers.validators.Validator
import shared.models.domain.TaxYear
import config.AppConfig
import play.api.libs.json.JsValue
import v4.amendUkPropertyPeriodSummary.AmendUkPropertyPeriodSummaryValidatorFactory.def2TaxYearStart
import v4.amendUkPropertyPeriodSummary.def1.Def1_AmendUkPropertyPeriodSummaryValidator
import v4.amendUkPropertyPeriodSummary.def2.Def2_AmendUkPropertyPeriodSummaryValidator
import v4.amendUkPropertyPeriodSummary.model.request.AmendUkPropertyPeriodSummaryRequestData

import javax.inject.Inject
import scala.math.Ordering.Implicits.infixOrderingOps

class AmendUkPropertyPeriodSummaryValidatorFactory @Inject() (appConfig: AppConfig) {

  def validator(
      nino: String,
      businessId: String,
      taxYear: String,
      submissionId: String,
      body: JsValue
  ): Validator[AmendUkPropertyPeriodSummaryRequestData] = {

    TaxYear.maybeFromMtd(taxYear) match {
      case Some(parsedTY) if parsedTY >= def2TaxYearStart =>
        new Def2_AmendUkPropertyPeriodSummaryValidator(nino, businessId, taxYear, submissionId, body)

      case _ =>
        new Def1_AmendUkPropertyPeriodSummaryValidator(nino, businessId, taxYear, submissionId, body)(appConfig)
    }
  }

}

object AmendUkPropertyPeriodSummaryValidatorFactory {

  private val def2TaxYearStart = TaxYear.fromMtd("2024-25")

}
