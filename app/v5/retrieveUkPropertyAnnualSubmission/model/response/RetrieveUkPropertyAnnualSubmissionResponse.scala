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

package v5.retrieveUkPropertyAnnualSubmission.model.response

import play.api.libs.json._
import shared.utils.JsonWritesUtil.writesFrom
import v5.retrieveUkPropertyAnnualSubmission.def1.model.response.Def1_RetrieveUkPropertyAnnualSubmissionResponse
import v5.retrieveUkPropertyAnnualSubmission.def2.model.response.Def2_RetrieveUkPropertyAnnualSubmissionResponse

trait RetrieveUkPropertyAnnualSubmissionResponse {
  def hasUkData: Boolean
}

object RetrieveUkPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[RetrieveUkPropertyAnnualSubmissionResponse] = writesFrom {
    case def1: Def1_RetrieveUkPropertyAnnualSubmissionResponse => implicitly[OWrites[Def1_RetrieveUkPropertyAnnualSubmissionResponse]].writes(def1)
    case def2: Def2_RetrieveUkPropertyAnnualSubmissionResponse => implicitly[OWrites[Def2_RetrieveUkPropertyAnnualSubmissionResponse]].writes(def2)
  }

}
