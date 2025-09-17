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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v5.createAmendUkPropertyAnnualSubmission.def1.model.request.ukFhlProperty.CreateAmendUkFhlProperty
import v5.createAmendUkPropertyAnnualSubmission.def1.model.request.ukProperty.CreateAmendUkProperty
import v5.createAmendUkPropertyAnnualSubmission.model.request.CreateAmendUkPropertyAnnualSubmissionRequestBody

case class Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody(ukFhlProperty: Option[CreateAmendUkFhlProperty],
                                                                 ukProperty: Option[CreateAmendUkProperty])
    extends CreateAmendUkPropertyAnnualSubmissionRequestBody

object Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody {
  implicit val reads: Reads[Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody] = Json.reads[Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody]

  implicit val writes: OWrites[Def1_CreateAmendUkPropertyAnnualSubmissionRequestBody] = (
    (JsPath \ "ukFhlProperty").writeNullable[CreateAmendUkFhlProperty] and
      (JsPath \ "ukOtherProperty").writeNullable[CreateAmendUkProperty]
  )(o => Tuple.fromProductTyped(o))

}
