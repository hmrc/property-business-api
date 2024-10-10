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

package v5.createForeignPropertyPeriodCumulativeSummary.model.request

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shapeless.HNil
import utils.EmptinessChecker
import v5.createForeignPropertyPeriodCumulativeSummary.def1.model.request.Def1_foreignPropertyEntry.Def1_Create_CreateForeignPropertyEntry

sealed trait CreateForeignPropertyPeriodCumulativeSummaryRequestBody

case class Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody(fromDate: String,
                                                                        toDate: String,
                                                                        foreignProperty: Option[Seq[Def1_Create_CreateForeignPropertyEntry]])
    extends CreateForeignPropertyPeriodCumulativeSummaryRequestBody

object Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody {

  implicit val emptinessChecker: EmptinessChecker[Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody] = EmptinessChecker.use { body =>
    "foreignProperty" -> body.foreignProperty :: HNil
  }

  implicit val reads: Reads[Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody] =
    Json.reads[Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody]

  implicit val writes: OWrites[Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody] = (
    (JsPath \ "fromDate").write[String] and
      (JsPath \ "toDate").write[String] and
      (JsPath \ "foreignProperty").writeNullable[Seq[Def1_Create_CreateForeignPropertyEntry]]
  )(unlift(Def1_CreateForeignPropertyPeriodCumulativeSummaryRequestBody.unapply))

}
