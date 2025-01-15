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

package v5.retrieveForeignPropertyAnnualSubmission.model.response

import play.api.libs.json.OWrites
import shared.utils.JsonWritesUtil.writesFrom
import v5.retrieveForeignPropertyAnnualSubmission.def1.model.response.Def1_RetrieveForeignPropertyAnnualSubmissionResponse
import v5.retrieveForeignPropertyAnnualSubmission.def2.model.response.Def2_RetrieveForeignPropertyAnnualSubmissionResponse

trait RetrieveForeignPropertyAnnualSubmissionResponse {
  def hasForeignData: Boolean
}

object RetrieveForeignPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[RetrieveForeignPropertyAnnualSubmissionResponse] = writesFrom {
    case def1: Def1_RetrieveForeignPropertyAnnualSubmissionResponse =>
      implicitly[OWrites[Def1_RetrieveForeignPropertyAnnualSubmissionResponse]].writes(def1)
    case def2: Def2_RetrieveForeignPropertyAnnualSubmissionResponse =>
      implicitly[OWrites[Def2_RetrieveForeignPropertyAnnualSubmissionResponse]].writes(def2)
  }

}
