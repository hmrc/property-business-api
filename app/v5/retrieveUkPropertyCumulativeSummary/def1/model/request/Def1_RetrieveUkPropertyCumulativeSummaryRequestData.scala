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

package v5.retrieveUkPropertyCumulativeSummary.def1.model.request

import shared.models.domain.{BusinessId, Nino, TaxYear}
import v5.retrieveUkPropertyCumulativeSummary.RetrieveUkPropertyCumulativeSummarySchema
import v5.retrieveUkPropertyCumulativeSummary.model.request.RetrieveUkPropertyCumulativeSummaryRequestData

case class Def1_RetrieveUkPropertyCumulativeSummaryRequestData(nino: Nino, businessId: BusinessId, taxYear: TaxYear)
    extends RetrieveUkPropertyCumulativeSummaryRequestData {
  override val schema: RetrieveUkPropertyCumulativeSummarySchema = RetrieveUkPropertyCumulativeSummarySchema.Def1
}
