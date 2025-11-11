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

package v6.retrieveForeignPropertyDetails.model.response

import play.api.libs.json.OWrites
import shared.utils.JsonWritesUtil.writesFrom
import v6.retrieveForeignPropertyDetails.def1.model.response.Def1_RetrieveForeignPropertyDetailsResponse

trait RetrieveForeignPropertyDetailsResponse {}

object RetrieveForeignPropertyDetailsResponse {

  implicit val writes: OWrites[RetrieveForeignPropertyDetailsResponse] = writesFrom { case def1: Def1_RetrieveForeignPropertyDetailsResponse =>
    implicitly[OWrites[Def1_RetrieveForeignPropertyDetailsResponse]].writes(def1)
  }

}
