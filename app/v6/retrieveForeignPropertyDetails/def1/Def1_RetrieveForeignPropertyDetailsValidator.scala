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

package v6.retrieveForeignPropertyDetails.def1

import cats.data.Validated
import cats.syntax.all.*
import common.controllers.validators.resolvers.ResolveUuid
import common.models.domain.PropertyId
import common.models.errors.PropertyIdFormatError
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino}
import shared.models.errors.MtdError
import shared.models.domain.TaxYear
import v6.retrieveForeignPropertyDetails.def1.model.request.Def1_RetrieveForeignPropertyDetailsRequestData
import v6.retrieveForeignPropertyDetails.model.request.RetrieveForeignPropertyDetailsRequestData
import javax.inject.Inject

class Def1_RetrieveForeignPropertyDetailsValidator @Inject() (
    nino: String,
    businessId: String,
    taxYear: String,
    propertyId: Option[String]
) extends Validator[RetrieveForeignPropertyDetailsRequestData] {

  override def validate: Validated[Seq[MtdError], RetrieveForeignPropertyDetailsRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveUuid(propertyId, PropertyIdFormatError)(PropertyId.apply)
    ).mapN { (validNino, validBusinessId, validPropertyId) =>
      Def1_RetrieveForeignPropertyDetailsRequestData(
        nino = validNino,
        businessId = validBusinessId,
        taxYear = TaxYear.fromMtd(taxYear),
        propertyId = validPropertyId
      )
    }

}
