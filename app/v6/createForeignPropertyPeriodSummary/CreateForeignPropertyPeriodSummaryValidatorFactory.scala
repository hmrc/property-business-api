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

package v6.createForeignPropertyPeriodSummary

import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.models.domain.TaxYear
import v6.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryValidatorFactory.{def2TaxYearStart, maximumTaxYear}
import v6.createForeignPropertyPeriodSummary.def1.Def1_CreateForeignPropertyPeriodSummaryValidator
import v6.createForeignPropertyPeriodSummary.def2.Def2_CreateForeignPropertyPeriodSummaryValidator
import v6.createForeignPropertyPeriodSummary.model.request.CreateForeignPropertyPeriodSummaryRequestData

import javax.inject.Singleton
import scala.math.Ordering.Implicits.infixOrderingOps

@Singleton
class CreateForeignPropertyPeriodSummaryValidatorFactory {

  def validator(nino: String, businessId: String, taxYear: String, body: JsValue): Validator[CreateForeignPropertyPeriodSummaryRequestData] = {

    TaxYear.maybeFromMtd(taxYear) match {
      case Some(parsedTY) if parsedTY >= def2TaxYearStart =>
        new Def2_CreateForeignPropertyPeriodSummaryValidator(nino, businessId, taxYear, maximumTaxYear, body)

      case _ =>
        new Def1_CreateForeignPropertyPeriodSummaryValidator(nino, businessId, taxYear, maximumTaxYear, body)
    }
  }

}

object CreateForeignPropertyPeriodSummaryValidatorFactory {
  private val maximumTaxYear   = TaxYear.fromMtd("2024-25")
  private val def2TaxYearStart = TaxYear.fromMtd("2024-25")

}
