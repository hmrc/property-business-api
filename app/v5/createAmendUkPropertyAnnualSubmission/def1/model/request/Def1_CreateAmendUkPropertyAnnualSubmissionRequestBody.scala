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

package v5.createAmendUkPropertyAnnualSubmission.def1.model.request

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v5.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukFhlProperty.Def1_Create_Amend_UkFhlProperty
import v5.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukNonFhlProperty.Def1_Create_Amend_UkNonFhlProperty
import v5.createAmendUkPropertyAnnualSubmission.model.request.CreateAmendUkPropertyAnnualSubmissionRequestBody

case class Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody(ukFhlProperty: Option[Def1_Create_Amend_UkFhlProperty],
                                                                 ukNonFhlProperty: Option[Def1_Create_Amend_UkNonFhlProperty])
    extends CreateAmendUkPropertyAnnualSubmissionRequestBody

object Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody {
  implicit val reads: Reads[Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody] = Json.reads[Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody]

  implicit val writes: OWrites[Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody] = (
    (JsPath \ "ukFhlProperty").writeNullable[Def1_Create_Amend_UkFhlProperty] and
      (JsPath \ "ukOtherProperty").writeNullable[Def1_Create_Amend_UkNonFhlProperty]
  )(unlift(Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody.unapply))

}
