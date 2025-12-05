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

package v6.retrieveForeignPropertyCumulativeSummary.def2

import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import common.controllers.validators.resolvers.ResolveUuid
import common.models.domain.PropertyId
import common.models.errors.PropertyIdFormatError
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v6.retrieveForeignPropertyCumulativeSummary.def2.model.request.Def2_RetrieveForeignPropertyCumulativeSummaryRequestData
import v6.retrieveForeignPropertyCumulativeSummary.model.request.RetrieveForeignPropertyCumulativeSummaryRequestData

import javax.inject.Inject

class Def2_RetrieveForeignPropertyCumulativeSummaryValidator @Inject() (
    nino: String,
    businessId: String,
    taxYear: String,
    propertyId: Option[String]
) extends Validator[RetrieveForeignPropertyCumulativeSummaryRequestData] {

  override def validate: Validated[Seq[MtdError], RetrieveForeignPropertyCumulativeSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveUuid(propertyId, PropertyIdFormatError)(PropertyId.apply)
    ).mapN { (validNino, validBusinessId, validPropertyId) =>
      Def2_RetrieveForeignPropertyCumulativeSummaryRequestData.apply(
        nino = validNino,
        businessId = validBusinessId,
        taxYear = TaxYear.fromMtd(taxYear),
        propertyId = validPropertyId
      )
    }

}
