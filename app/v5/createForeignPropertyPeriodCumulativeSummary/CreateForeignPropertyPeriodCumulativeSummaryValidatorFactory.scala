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

package v5.createForeignPropertyPeriodCumulativeSummary

import api.controllers.validators.Validator
import api.models.domain.TaxYear
import cats.data.Validated.{Invalid, Valid}
import config.AppConfig
import play.api.libs.json.JsValue
import v5.createForeignPropertyPeriodCumulativeSummary.CreateForeignPropertyPeriodCumulativeSummaryValidatorFactory.maximumTaxYear
import v5.createForeignPropertyPeriodCumulativeSummary.def1.Def1_CreateForeignPropertyPeriodCumulativeSummaryValidator
import v5.createForeignPropertyPeriodCumulativeSummary.model.request.CreateForeignPropertyPeriodCumulativeSummaryRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class CreateForeignPropertyPeriodCumulativeSummaryValidatorFactory @Inject() (appConfig: AppConfig) {

  def validator(nino: String, businessId: String, taxYear: String, body: JsValue): Validator[CreateForeignPropertyPeriodCumulativeSummaryRequestData] =
    CreateForeignPropertyPeriodCumulativeSummarySchema.schemaFor(taxYear) match {
      case Valid(CreateForeignPropertyPeriodCumulativeSummarySchema.Def1) =>
        new Def1_CreateForeignPropertyPeriodCumulativeSummaryValidator(
          nino,
          businessId,
          taxYear,
          maximumTaxYear,
          body,
          appConfig
        )
      case Invalid(errors) => Validator.returningErrors(errors)
    }
}

object CreateForeignPropertyPeriodCumulativeSummaryValidatorFactory {
  private val maximumTaxYear   = TaxYear.fromMtd("2025-26")

}
