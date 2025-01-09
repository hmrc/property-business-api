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

package v3.models.request.createForeignPropertyPeriodSummary

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shapeless.HNil
import shared.utils.EmptinessChecker
import v3.models.request.createForeignPropertyPeriodSummary.foreignFhlEea.CreateForeignFhlEea
import v3.models.request.createForeignPropertyPeriodSummary.foreignPropertyEntry.CreateForeignNonFhlPropertyEntry

case class CreateForeignPropertyPeriodSummaryRequestBody(fromDate: String,
                                                         toDate: String,
                                                         foreignFhlEea: Option[CreateForeignFhlEea],
                                                         foreignNonFhlProperty: Option[Seq[CreateForeignNonFhlPropertyEntry]])

object CreateForeignPropertyPeriodSummaryRequestBody {

  implicit val emptinessChecker: EmptinessChecker[CreateForeignPropertyPeriodSummaryRequestBody] = EmptinessChecker.use { body =>
    "foreignFhlEea"           -> body.foreignFhlEea ::
      "foreignNonFhlProperty" -> body.foreignNonFhlProperty :: HNil
  }

  implicit val reads: Reads[CreateForeignPropertyPeriodSummaryRequestBody] = Json.reads[CreateForeignPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[CreateForeignPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "fromDate").write[String] and
      (JsPath \ "toDate").write[String] and
      (JsPath \ "foreignFhlEea").writeNullable[CreateForeignFhlEea] and
      (JsPath \ "foreignProperty").writeNullable[Seq[CreateForeignNonFhlPropertyEntry]]
  )(unlift(CreateForeignPropertyPeriodSummaryRequestBody.unapply))

}
