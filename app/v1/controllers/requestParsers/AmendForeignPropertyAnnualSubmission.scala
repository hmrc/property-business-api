/*
 *   Copyright 2020 HM Revenue & Customs
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package v1.controllers.requestParsers

import javax.inject.Inject
import uk.gov.hmrc.domain.Nino

class AmendForeignPropertyAnnualSubmission @Inject()(val validator: AmendForeignPropertyAnnualSubmissionValidator)
  extends RequestParser[RawData???, Request???] {

  override protected def requestFor(data: RawData???): Request??? =
    Request???(Nino(data.nino), data.businessId, data.taxYear, data.body.as[RequestBody???])

}
