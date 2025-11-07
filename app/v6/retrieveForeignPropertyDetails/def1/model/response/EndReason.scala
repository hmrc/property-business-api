/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.retrieveForeignPropertyDetails.def1.model.response

import play.api.libs.json.Format
import shared.utils.enums.Enums

enum EndReason(val fromDownstream: String) {
  case `no-longer-renting-property-out` extends EndReason("noLongerRentingPropertyOut")
  case `disposal`                       extends EndReason("disposal")
  case `added-in-error`                 extends EndReason("addedInError")
  case `cessation`                      extends EndReason("cessation")
}

object EndReason {

  given Format[EndReason]                        = Enums.format(values)
  val parser: PartialFunction[String, EndReason] = Enums.parser[EndReason](values)
}
