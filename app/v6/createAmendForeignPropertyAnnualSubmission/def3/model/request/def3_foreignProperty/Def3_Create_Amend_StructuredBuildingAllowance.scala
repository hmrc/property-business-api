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

package v6.createAmendForeignPropertyAnnualSubmission.def3.model.request.def3_foreignProperty

import play.api.libs.json.{Json, OFormat}

case class Def3_Create_Amend_StructuredBuildingAllowance(amount: BigDecimal,
                                                         firstYear: Option[Def3_Create_Amend_FirstYear],
                                                         building: Def3_Create_Amend_Building)

object Def3_Create_Amend_StructuredBuildingAllowance {
  implicit val format: OFormat[Def3_Create_Amend_StructuredBuildingAllowance] = Json.format[Def3_Create_Amend_StructuredBuildingAllowance]
}
