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

package v4.controllers.createUkPropertyPeriodSummary.model.request

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shapeless.HNil
import utils.EmptinessChecker
import v4.controllers.createUkPropertyPeriodSummary.def1.model.request.ukFhlProperty.UkFhlProperty
import v4.controllers.createUkPropertyPeriodSummary.def1.model.request.ukNonFhlProperty.UkNonFhlProperty

case class CreateUkPropertyPeriodSummaryRequestBody(fromDate: String,
                                                    toDate: String,
                                                    ukFhlProperty: Option[UkFhlProperty],
                                                    ukNonFhlProperty: Option[UkNonFhlProperty])

object CreateUkPropertyPeriodSummaryRequestBody {

  implicit val emptinessChecker: EmptinessChecker[CreateUkPropertyPeriodSummaryRequestBody] = EmptinessChecker.use { body =>
    "ukFhlProperty"      -> body.ukFhlProperty ::
      "ukNonFhlProperty" -> body.ukNonFhlProperty :: HNil
  }

  implicit val reads: Reads[CreateUkPropertyPeriodSummaryRequestBody] = Json.reads[CreateUkPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[CreateUkPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "fromDate").write[String] and
      (JsPath \ "toDate").write[String] and
      (JsPath \ "ukFhlProperty").writeNullable[UkFhlProperty] and
      (JsPath \ "ukOtherProperty").writeNullable[UkNonFhlProperty]
  )(unlift(CreateUkPropertyPeriodSummaryRequestBody.unapply))

}
