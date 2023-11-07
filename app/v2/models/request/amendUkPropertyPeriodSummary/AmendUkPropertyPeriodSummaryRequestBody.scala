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

package v2.models.request.amendUkPropertyPeriodSummary

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v2.models.request.amendUkPropertyPeriodSummary.ukFhlProperty.UkFhlProperty
import v2.models.request.amendUkPropertyPeriodSummary.ukNonFhlProperty.UkNonFhlProperty

case class AmendUkPropertyPeriodSummaryRequestBody(ukFhlProperty: Option[UkFhlProperty], ukNonFhlProperty: Option[UkNonFhlProperty])

object AmendUkPropertyPeriodSummaryRequestBody {
  implicit val reads: Reads[AmendUkPropertyPeriodSummaryRequestBody] = Json.reads[AmendUkPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[AmendUkPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "ukFhlProperty").writeNullable[UkFhlProperty] and
      (JsPath \ "ukOtherProperty").writeNullable[UkNonFhlProperty]
  )(unlift(AmendUkPropertyPeriodSummaryRequestBody.unapply))

}
