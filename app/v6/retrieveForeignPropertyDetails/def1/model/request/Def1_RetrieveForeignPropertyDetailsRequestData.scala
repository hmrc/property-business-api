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

package v6.retrieveForeignPropertyDetails.def1.model.request

import common.models.domain.PropertyId
import shared.models.domain.{BusinessId, Nino, TaxYear}
import v6.retrieveForeignPropertyDetails.RetrieveForeignPropertyDetailsSchema
import v6.retrieveForeignPropertyDetails.model.request.RetrieveForeignPropertyDetailsRequestData

case class Def1_RetrieveForeignPropertyDetailsRequestData(nino: Nino, businessId: BusinessId, taxYear: TaxYear, propertyId: PropertyId)
    extends RetrieveForeignPropertyDetailsRequestData {
  override val schema: RetrieveForeignPropertyDetailsSchema = RetrieveForeignPropertyDetailsSchema.Def1
}
