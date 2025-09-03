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

package v4.amendForeignPropertyPeriodSummary.model.request

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v4.amendForeignPropertyPeriodSummary.def1.model.request.foreignFhlEea.AmendForeignFhlEea
import v4.amendForeignPropertyPeriodSummary.def1.model.request.foreignPropertyEntry.AmendForeignNonFhlPropertyEntry
import v4.amendForeignPropertyPeriodSummary.def2.model.request.def2_foreignFhlEea.Def2_AmendForeignFhlEea
import v4.amendForeignPropertyPeriodSummary.def2.model.request.def2_foreignPropertyEntry.Def2_AmendForeignNonFhlPropertyEntry

sealed trait AmendForeignPropertyPeriodSummaryRequestBody

case class Def1_AmendForeignPropertyPeriodSummaryRequestBody(foreignFhlEea: Option[AmendForeignFhlEea],
                                                             foreignNonFhlProperty: Option[Seq[AmendForeignNonFhlPropertyEntry]])
    extends AmendForeignPropertyPeriodSummaryRequestBody

object Def1_AmendForeignPropertyPeriodSummaryRequestBody {
  implicit val reads: Reads[Def1_AmendForeignPropertyPeriodSummaryRequestBody] = Json.reads[Def1_AmendForeignPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[Def1_AmendForeignPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "foreignFhlEea").writeNullable[AmendForeignFhlEea] and
      (JsPath \ "foreignProperty").writeNullable[Seq[AmendForeignNonFhlPropertyEntry]]
  )(o => Tuple.fromProductTyped(o))

}

case class Def2_AmendForeignPropertyPeriodSummaryRequestBody(foreignFhlEea: Option[Def2_AmendForeignFhlEea],
                                                             foreignNonFhlProperty: Option[Seq[Def2_AmendForeignNonFhlPropertyEntry]])
    extends AmendForeignPropertyPeriodSummaryRequestBody

object Def2_AmendForeignPropertyPeriodSummaryRequestBody {
  implicit val reads: Reads[Def2_AmendForeignPropertyPeriodSummaryRequestBody] = Json.reads[Def2_AmendForeignPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[Def2_AmendForeignPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "foreignFhlEea").writeNullable[Def2_AmendForeignFhlEea] and
      (JsPath \ "foreignProperty").writeNullable[Seq[Def2_AmendForeignNonFhlPropertyEntry]]
  )(o => Tuple.fromProductTyped(o))

}
