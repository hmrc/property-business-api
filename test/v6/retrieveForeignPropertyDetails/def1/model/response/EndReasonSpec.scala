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

import shared.utils.UnitSpec
import shared.utils.enums.EnumJsonSpecSupport
import v6.retrieveForeignPropertyDetails.def1.model.response.EndReason.*

class EndReasonSpec extends UnitSpec with EnumJsonSpecSupport {

  testDeserialization(
    ("noLongerRentingPropertyOut", `no-longer-renting-property-out`),
    ("disposal", disposal),
    ("addedInError", `added-in-error`),
    ("cessation", cessation)
  )

  testSerialization(
    (`no-longer-renting-property-out`, "no-longer-renting-property-out"),
    (disposal, "disposal"),
    (`added-in-error`, "added-in-error"),
    (cessation, "cessation")
  )

}
