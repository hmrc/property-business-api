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

package v2.controllers.requestParsers

import play.api.libs.json.Json
import support.UnitSpec
import v2.mocks.validators.MockAmendForeignPropertyPeriodSummaryValidator
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.request.amendForeignPropertyPeriodSummary._
import v2.models.request.common.foreignFhlEea._

class AmendHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRequestParserSpec extends UnitSpec {

  private val nino                   = "AA123456A"
  private val periodId               = "2017-04-06_2017-07-04"
  implicit val correlationId: String = "X-123"

}
