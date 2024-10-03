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

package v3.models.request.amendUkPropertyAnnualSubmission

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v3.models.request.amendUkPropertyAnnualSubmission.ukFhlProperty.UkFhlProperty
import v3.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty.UkNonFhlProperty

case class AmendUkPropertyAnnualSubmissionRequestBody(ukFhlProperty: Option[UkFhlProperty], ukNonFhlProperty: Option[UkNonFhlProperty])

object AmendUkPropertyAnnualSubmissionRequestBody {
  implicit val reads: Reads[AmendUkPropertyAnnualSubmissionRequestBody] = Json.reads[AmendUkPropertyAnnualSubmissionRequestBody]

  implicit val writes: OWrites[AmendUkPropertyAnnualSubmissionRequestBody] = (
    (JsPath \ "ukFhlProperty").writeNullable[UkFhlProperty] and
      (JsPath \ "ukOtherProperty").writeNullable[UkNonFhlProperty]
  )(unlift(AmendUkPropertyAnnualSubmissionRequestBody.unapply))

}
