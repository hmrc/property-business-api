/*
 * Copyright 2021 HM Revenue & Customs
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

package v2.models.response.retrieveUkPropertyAnnualSummary

import play.api.libs.functional.syntax._
import play.api.libs.json._
import v2.models.response.retrieveUkPropertyAnnualSummary.ukFhlProperty.UkFhlProperty
import v2.models.response.retrieveUkPropertyAnnualSummary.ukNonFhlProperty.UkNonFhlProperty

case class RetrieveUkPropertyAnnualSummaryResponse(submittedOn: String,
                                                   ukFhlProperty: Option[UkFhlProperty],
                                                   ukNonFhlProperty: Option[UkNonFhlProperty])

object RetrieveUkPropertyAnnualSummaryResponse {
  implicit val writes: OWrites[RetrieveUkPropertyAnnualSummaryResponse] = Json.writes[RetrieveUkPropertyAnnualSummaryResponse]

  implicit val reads: Reads[RetrieveUkPropertyAnnualSummaryResponse] = (
    (__ \ "submittedOn").read[String] and
      (__ \ "ukFhlProperty").readNullable[UkFhlProperty] and
      (__ \ "ukOtherProperty").readNullable[UkNonFhlProperty]
  )(RetrieveUkPropertyAnnualSummaryResponse.apply _)
}
