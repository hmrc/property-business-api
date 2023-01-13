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

package v2.models.request.amendForeignPropertyPeriodSummary

import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Json, OWrites, Reads }
import v2.models.request.common.foreignFhlEea.AmendForeignFhlEea
import v2.models.request.common.foreignPropertyEntry.AmendForeignNonFhlPropertyEntry

case class AmendForeignPropertyPeriodSummaryRequestBody(foreignFhlEea: Option[AmendForeignFhlEea],
                                                        foreignNonFhlProperty: Option[Seq[AmendForeignNonFhlPropertyEntry]])

object AmendForeignPropertyPeriodSummaryRequestBody {
  implicit val reads: Reads[AmendForeignPropertyPeriodSummaryRequestBody] = Json.reads[AmendForeignPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[AmendForeignPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "foreignFhlEea").writeNullable[AmendForeignFhlEea] and
      (JsPath \ "foreignProperty").writeNullable[Seq[AmendForeignNonFhlPropertyEntry]]
  )(unlift(AmendForeignPropertyPeriodSummaryRequestBody.unapply))
}
