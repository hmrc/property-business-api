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

package v6.retrieveForeignPropertyPeriodSummary

import shared.controllers.validators.Validator
import shared.models.domain.TaxYear
import v6.retrieveForeignPropertyPeriodSummary.def1.Def1_RetrieveForeignPropertyPeriodSummaryValidator
import v6.retrieveForeignPropertyPeriodSummary.model.request._

import javax.inject.Singleton

@Singleton
class RetrieveForeignPropertyPeriodSummaryValidatorFactory {

  private val maximumTaxYear = TaxYear.fromMtd("2024-25")

  def validator(nino: String, businessId: String, taxYear: String, submissionId: String): Validator[RetrieveForeignPropertyPeriodSummaryRequestData] =
    new Def1_RetrieveForeignPropertyPeriodSummaryValidator(nino, businessId, taxYear, maximumTaxYear, submissionId)

}
