/*
 * Copyright 2023 HM Revenue & Customs
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

package v5.createAmendForeignPropertyAnnualSubmission.model.request

import play.api.libs.json._
import utils.JsonWritesUtil
import v5.createAmendForeignPropertyAnnualSubmission.def1.model.request.Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody
import v5.createAmendForeignPropertyAnnualSubmission.def2.model.request.Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody

trait CreateAmendForeignPropertyAnnualSubmissionRequestBody

object CreateAmendForeignPropertyAnnualSubmissionRequestBody extends JsonWritesUtil {

  implicit val writes: OWrites[CreateAmendForeignPropertyAnnualSubmissionRequestBody] = writesFrom {
    case def1: Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody =>
      implicitly[OWrites[Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody]].writes(def1)
    case def2: Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody =>
      implicitly[OWrites[Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody]].writes(def2)
  }

}
