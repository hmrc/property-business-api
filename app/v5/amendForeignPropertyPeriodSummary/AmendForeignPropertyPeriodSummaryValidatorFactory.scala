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

package v5.amendForeignPropertyPeriodSummary

import config.PropertyBusinessConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.models.domain.TaxYear
import v5.amendForeignPropertyPeriodSummary.AmendForeignPropertyPeriodSummaryValidatorFactory.{def2TaxYearStart, maximumTaxYear}
import v5.amendForeignPropertyPeriodSummary.def1.Def1_AmendForeignPropertyPeriodSummaryValidator
import v5.amendForeignPropertyPeriodSummary.def2.Def2_AmendForeignPropertyPeriodSummaryValidator
import v5.amendForeignPropertyPeriodSummary.model.request.AmendForeignPropertyPeriodSummaryRequestData

import javax.inject.{Inject, Singleton}
import scala.math.Ordering.Implicits.infixOrderingOps

@Singleton
class AmendForeignPropertyPeriodSummaryValidatorFactory @Inject() (implicit config: PropertyBusinessConfig) {

  def validator(nino: String,
                businessId: String,
                taxYear: String,
                submissionId: String,
                body: JsValue): Validator[AmendForeignPropertyPeriodSummaryRequestData] = {

    TaxYear.maybeFromMtd(taxYear) match {
      case Some(parsedTY) if parsedTY >= def2TaxYearStart =>
        new Def2_AmendForeignPropertyPeriodSummaryValidator(nino, businessId, taxYear, maximumTaxYear, submissionId, body)

      case _ =>
        new Def1_AmendForeignPropertyPeriodSummaryValidator(nino, businessId, taxYear, maximumTaxYear, submissionId, body)
    }
  }

}

object AmendForeignPropertyPeriodSummaryValidatorFactory {

  private val maximumTaxYear   = TaxYear.fromMtd("2024-25")
  private val def2TaxYearStart = TaxYear.fromMtd("2024-25")

}
