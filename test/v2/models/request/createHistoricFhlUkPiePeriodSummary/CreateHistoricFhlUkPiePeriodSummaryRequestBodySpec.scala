/*
 * Copyright 2022 HM Revenue & Customs
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

package v2.models.request.createHistoricFhlUkPiePeriodSummary

import support.UnitSpec
import v2.models.request.common.ukFhlProperty.{UkFhlProperty, UkFhlPropertyExpenses, UkFhlPropertyIncome}
import v2.models.request.common.ukPropertyRentARoom.UkPropertyIncomeRentARoom

class CreateHistoricFhlUkPiePeriodSummaryRequestBodySpec extends UnitSpec {

  val property:UkFhlProperty = UkFhlProperty(
    UkFhlPropertyIncome(Some(100.25), Some(100.15), Some(UkPropertyIncomeRentARoom(Some(97.50)))),
    UkFhlPropertyExpenses(Some(123.12),
      Some(17.90),
      Some(38.19),
      Some(13.42),
      Some(29.42),
      Some(751.00),
      Some(1259.18),
      Some(12.00),
      Some(UkPropertyIncomeRentARoom(Some(12.50)))))


  val requestBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
    CreateHistoricFhlUkPiePeriodSummaryRequestBody(
      "2017-04-06",
      "2017-07-05",
    )
}
