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

package v4.models.response.createAmendForeignPropertyAnnualSubmission

import api.hateoas.Link
import api.hateoas.Method.{DELETE, GET, PUT}
import mocks.MockAppConfig
import support.UnitSpec
import v4.controllers.createAmendForeignPropertyAnnualSubmission.model.response.{CreateAmendForeignPropertyAnnualSubmissionHateoasData, CreateAmendForeignPropertyAnnualSubmissionResponse}

class CreateAmendForeignPropertyAnnualSubmissionResponseSpec extends UnitSpec with MockAppConfig {

  "LinksFactory" should {

    "produce the correct links" in {
      val data: CreateAmendForeignPropertyAnnualSubmissionHateoasData =
        CreateAmendForeignPropertyAnnualSubmissionHateoasData(nino = "myNino", businessId = "myBusinessId", taxYear = "mySubmissionId")

      MockedAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()

      CreateAmendForeignPropertyAnnualSubmissionResponse.LinksFactory.links(mockAppConfig, data) shouldBe
        Seq(
          Link(
            s"/my/context/foreign/${data.nino}/${data.businessId}/annual/${data.taxYear}",
            PUT,
            "create-and-amend-foreign-property-annual-submission"),
          Link(s"/my/context/foreign/${data.nino}/${data.businessId}/annual/${data.taxYear}", GET, "self"),
          Link(s"/my/context/${data.nino}/${data.businessId}/annual/${data.taxYear}", DELETE, "delete-property-annual-submission")
        )
    }
  }

}
