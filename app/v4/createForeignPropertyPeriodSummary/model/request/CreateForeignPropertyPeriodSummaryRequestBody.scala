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

package v4.createForeignPropertyPeriodSummary.model.request

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shapeless.HNil
import shared.utils.EmptinessChecker
import v4.createForeignPropertyPeriodSummary.def1.model.request.Def1_foreignFhlEea.Def1_Create_CreateForeignFhlEea
import v4.createForeignPropertyPeriodSummary.def1.model.request.Def1_foreignPropertyEntry.Def1_Create_CreateForeignNonFhlPropertyEntry
import v4.createForeignPropertyPeriodSummary.def2.model.request.Def2_foreignFhlEea.Def2_Create_CreateForeignFhlEea
import v4.createForeignPropertyPeriodSummary.def2.model.request.Def2_foreignPropertyEntry.Def2_Create_CreateForeignNonFhlPropertyEntry

sealed trait CreateForeignPropertyPeriodSummaryRequestBody

case class Def1_CreateForeignPropertyPeriodSummaryRequestBody(fromDate: String,
                                                              toDate: String,
                                                              foreignFhlEea: Option[Def1_Create_CreateForeignFhlEea],
                                                              foreignNonFhlProperty: Option[Seq[Def1_Create_CreateForeignNonFhlPropertyEntry]])
    extends CreateForeignPropertyPeriodSummaryRequestBody

object Def1_CreateForeignPropertyPeriodSummaryRequestBody {

  implicit val emptinessChecker: EmptinessChecker[Def1_CreateForeignPropertyPeriodSummaryRequestBody] = EmptinessChecker.use { body =>
    "foreignFhlEea"           -> body.foreignFhlEea ::
      "foreignNonFhlProperty" -> body.foreignNonFhlProperty :: HNil
  }

  implicit val reads: Reads[Def1_CreateForeignPropertyPeriodSummaryRequestBody] = Json.reads[Def1_CreateForeignPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[Def1_CreateForeignPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "fromDate").write[String] and
      (JsPath \ "toDate").write[String] and
      (JsPath \ "foreignFhlEea").writeNullable[Def1_Create_CreateForeignFhlEea] and
      (JsPath \ "foreignProperty").writeNullable[Seq[Def1_Create_CreateForeignNonFhlPropertyEntry]]
  )(unlift(Def1_CreateForeignPropertyPeriodSummaryRequestBody.unapply))

}

case class Def2_CreateForeignPropertyPeriodSummaryRequestBody(fromDate: String,
                                                              toDate: String,
                                                              foreignFhlEea: Option[Def2_Create_CreateForeignFhlEea],
                                                              foreignNonFhlProperty: Option[Seq[Def2_Create_CreateForeignNonFhlPropertyEntry]])
    extends CreateForeignPropertyPeriodSummaryRequestBody

object Def2_CreateForeignPropertyPeriodSummaryRequestBody {

  implicit val emptinessChecker: EmptinessChecker[Def2_CreateForeignPropertyPeriodSummaryRequestBody] = EmptinessChecker.use { body =>
    "foreignFhlEea"           -> body.foreignFhlEea ::
      "foreignNonFhlProperty" -> body.foreignNonFhlProperty :: HNil
  }

  implicit val reads: Reads[Def2_CreateForeignPropertyPeriodSummaryRequestBody] = Json.reads[Def2_CreateForeignPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[Def2_CreateForeignPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "fromDate").write[String] and
      (JsPath \ "toDate").write[String] and
      (JsPath \ "foreignFhlEea").writeNullable[Def2_Create_CreateForeignFhlEea] and
      (JsPath \ "foreignProperty").writeNullable[Seq[Def2_Create_CreateForeignNonFhlPropertyEntry]]
  )(unlift(Def2_CreateForeignPropertyPeriodSummaryRequestBody.unapply))

}
