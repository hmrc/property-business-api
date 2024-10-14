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

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v5.createAmendUkPropertyCumulativeSummary.def1.model.request.UkProperty

sealed trait CreateAmendUkPropertyCumulativeSummaryRequestBody

case class Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody(
    fromDate: String,
    toDate: String,
    ukProperty: UkProperty
) extends CreateAmendUkPropertyCumulativeSummaryRequestBody

object Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody {

  implicit val reads: Reads[Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody] =
    Json.reads[Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody]

  implicit val writes: OWrites[Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody] = (
    (JsPath \ "fromDate").write[String] and
      (JsPath \ "toDate").write[String] and
      (JsPath \ "ukOtherProperty").write[UkProperty]
  )(unlift(Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody.unapply))

}
