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

package v6.createAmendUkPropertyAnnualSubmission.def2.model.request

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v6.createAmendUkPropertyAnnualSubmission.model.request.CreateAmendUkPropertyAnnualSubmissionRequestBody

case class Def2_CreateAmendUkPropertyAnnualSubmissionRequestBody(ukProperty: UkProperty) extends CreateAmendUkPropertyAnnualSubmissionRequestBody

object Def2_CreateAmendUkPropertyAnnualSubmissionRequestBody {

  implicit val reads: Reads[Def2_CreateAmendUkPropertyAnnualSubmissionRequestBody] = Json.reads[Def2_CreateAmendUkPropertyAnnualSubmissionRequestBody]

  implicit val writes: OWrites[Def2_CreateAmendUkPropertyAnnualSubmissionRequestBody] =
    (JsPath \ "ukOtherProperty").write[UkProperty].contramap[Def2_CreateAmendUkPropertyAnnualSubmissionRequestBody](_.ukProperty)

}
