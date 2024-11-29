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

package v5.createAmendUkPropertyCumulativeSummary.model.request

import play.api.libs.json.OWrites
import utils.JsonWritesUtil.writesFrom
import v5.createAmendUkPropertyCumulativeSummary.def1.model.request.Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody

trait CreateAmendUkPropertyCumulativeSummaryRequestBody

object CreateAmendUkPropertyCumulativeSummaryRequestBody {

  implicit val writes: OWrites[CreateAmendUkPropertyCumulativeSummaryRequestBody] = writesFrom {
    case def1: Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody =>
      implicitly[OWrites[Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody]].writes(def1)

  }

}