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

package v6.retrieveForeignPropertyCumulativeSummary.model.response

import play.api.libs.json.OWrites
import shared.utils.JsonWritesUtil.writesFrom
import v6.retrieveForeignPropertyCumulativeSummary.def1.model.response.Def1_RetrieveForeignPropertyCumulativeSummaryResponse
import v6.retrieveForeignPropertyCumulativeSummary.def2.model.response.Def2_RetrieveForeignPropertyCumulativeSummaryResponse

trait RetrieveForeignPropertyCumulativeSummaryResponse {
  def hasForeignData: Boolean
}

object RetrieveForeignPropertyCumulativeSummaryResponse {

  implicit val writes: OWrites[RetrieveForeignPropertyCumulativeSummaryResponse] = writesFrom {
    case def1: Def1_RetrieveForeignPropertyCumulativeSummaryResponse =>
      implicitly[OWrites[Def1_RetrieveForeignPropertyCumulativeSummaryResponse]].writes(def1)

    case def2: Def2_RetrieveForeignPropertyCumulativeSummaryResponse =>
      implicitly[OWrites[Def2_RetrieveForeignPropertyCumulativeSummaryResponse]].writes(def2)
  }

}
