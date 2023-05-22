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

package v2.hateoas

import api.models.hateoas.{Link, Method}
import api.models.hateoas.Method._
import mocks.MockAppConfig
import support.UnitSpec
import v2.models.response.retrieveHistoricNonFhlUkPiePeriodSummary.{
  RetrieveHistoricNonFhlUkPiePeriodSummaryHateoasData,
  RetrieveHistoricNonFhlUkPiePeriodSummaryResponse
}

class HateoasLinksSpec extends UnitSpec with MockAppConfig with HateoasLinks {

  val nino         = "{nino}"
  val businessId   = "{businessId}"
  val taxYear      = "{taxYear}"
  val submissionId = "{submissionId}"
  val periodId     = "{periodId}"

  class Test {
    MockAppConfig.apiGatewayContext.returns("individuals/business/property").anyNumberOfTimes()
  }

  "The HateoasLinks functions" when {
    "for foreign property endpoints" must {
      "work for FP1 'Create FP period summary'" in new Test {
        createForeignPropertyPeriodSummary(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear) shouldBe
          Link("/individuals/business/property/foreign/{nino}/{businessId}/period/{taxYear}", POST, "create-foreign-property-period-summary")
      }

      "work for FP3 'Amend FP Period Summary'" in new Test {
        amendForeignPropertyPeriodSummary(
          mockAppConfig,
          nino = nino,
          businessId = businessId,
          taxYear = taxYear,
          submissionId = submissionId) shouldBe
          Link(
            "/individuals/business/property/foreign/{nino}/{businessId}/period/{taxYear}/{submissionId}",
            PUT,
            "amend-foreign-property-period-summary")
      }

      "work for FP4 'Retrieve FP Period Summary'" in new Test {
        retrieveForeignPropertyPeriodSummary(
          mockAppConfig,
          nino = nino,
          businessId = businessId,
          taxYear = taxYear,
          submissionId = submissionId,
          self = true) shouldBe
          Link("/individuals/business/property/foreign/{nino}/{businessId}/period/{taxYear}/{submissionId}", GET, "self")

        retrieveForeignPropertyPeriodSummary(
          mockAppConfig,
          nino = nino,
          businessId = businessId,
          taxYear = taxYear,
          submissionId = submissionId,
          self = false) shouldBe
          Link(
            "/individuals/business/property/foreign/{nino}/{businessId}/period/{taxYear}/{submissionId}",
            GET,
            "retrieve-foreign-property-period-summary")
      }

      "work for FP5 'Create and Amend FP Annual Submission'" in new Test {
        createAmendForeignPropertyAnnualSubmission(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear) shouldBe
          Link(
            "/individuals/business/property/foreign/{nino}/{businessId}/annual/{taxYear}",
            PUT,
            "create-and-amend-foreign-property-annual-submission")
      }

      "work for FP6 'Retrieve FP Annual Submission'" in new Test {
        retrieveForeignPropertyAnnualSubmission(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear, self = true) shouldBe
          Link("/individuals/business/property/foreign/{nino}/{businessId}/annual/{taxYear}", GET, "self")

        retrieveForeignPropertyAnnualSubmission(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear, self = false) shouldBe
          Link("/individuals/business/property/foreign/{nino}/{businessId}/annual/{taxYear}", GET, "retrieve-foreign-property-annual-submission")
      }
    }

    "for UK property endpoints" must {
      "work for UKP1 'Create UK Period Summary'" in new Test {
        createUkPropertyPeriodSummary(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear) shouldBe
          Link("/individuals/business/property/uk/{nino}/{businessId}/period/{taxYear}", POST, "create-uk-property-period-summary")
      }

      "work for UKP3 'Amend UK Period Summary'" in new Test {
        amendUkPropertyPeriodSummary(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear, submissionId = submissionId) shouldBe
          Link("/individuals/business/property/uk/{nino}/{businessId}/period/{taxYear}/{submissionId}", PUT, "amend-uk-property-period-summary")
      }

      "work for UKP4 'Retrieve UK Period Summary'" in new Test {
        retrieveUkPropertyPeriodSummary(
          mockAppConfig,
          nino = nino,
          businessId = businessId,
          taxYear = taxYear,
          submissionId = submissionId,
          self = true) shouldBe
          Link("/individuals/business/property/uk/{nino}/{businessId}/period/{taxYear}/{submissionId}", GET, "self")

        retrieveUkPropertyPeriodSummary(
          mockAppConfig,
          nino = nino,
          businessId = businessId,
          taxYear = taxYear,
          submissionId = submissionId,
          self = false) shouldBe
          Link("/individuals/business/property/uk/{nino}/{businessId}/period/{taxYear}/{submissionId}", GET, "retrieve-uk-property-period-summary")
      }

      "work for UKP5 'Create and Amend UK Annual Submission'" in new Test {
        createAmendUkPropertyAnnualSubmission(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear) shouldBe
          Link("/individuals/business/property/uk/{nino}/{businessId}/annual/{taxYear}", PUT, "create-and-amend-uk-property-annual-submission")
      }

      "work for UKP6 'Retrieve UK Annual Submission'" in new Test {
        retrieveUkPropertyAnnualSubmission(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear, self = true) shouldBe
          Link("/individuals/business/property/uk/{nino}/{businessId}/annual/{taxYear}", GET, "self")

        retrieveUkPropertyAnnualSubmission(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear, self = false) shouldBe
          Link("/individuals/business/property/uk/{nino}/{businessId}/annual/{taxYear}", GET, "retrieve-uk-property-annual-submission")
      }
    }

    "for generic property endpoints" must {
      "work for L2 'List Period Summaries'" in new Test {
        listPropertyPeriodSummaries(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear, self = true) shouldBe
          Link("/individuals/business/property/{nino}/{businessId}/period/{taxYear}", GET, "self")

        listPropertyPeriodSummaries(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear, self = false) shouldBe
          Link("/individuals/business/property/{nino}/{businessId}/period/{taxYear}", GET, "list-property-period-summaries")
      }

      "work for D7 'Delete Annual Submission'" in new Test {
        deletePropertyAnnualSubmission(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear) shouldBe
          Link("/individuals/business/property/{nino}/{businessId}/annual/{taxYear}", DELETE, "delete-property-annual-submission")
      }
    }

    "for a Historic Uk Property Income & Expenses (PIE) Period Summary" must {
      "return the HATEOAS link for Retrieve" in new Test {
        val result: Link = retrieveHistoricFhlUkPiePeriodSummary(mockAppConfig, nino = nino, periodId = periodId)

        result shouldBe
          Link("/individuals/business/property/uk/period/furnished-holiday-lettings/{nino}/{periodId}", GET, "self")

      }

      "return the HATEOAS link for Amend" in new Test {
        val result: Link = amendHistoricFhlUkPiePeriodSummary(mockAppConfig, nino = nino, periodId = periodId)

        result shouldBe
          Link(
            "/individuals/business/property/uk/period/furnished-holiday-lettings/{nino}/{periodId}",
            PUT,
            "amend-uk-property-historic-fhl-period-summary")
      }
    }

    "for a Historic Uk Property Annual Submission" must {
      "fhl" must {
        "return the HATEOAS link for retrieve" in new Test {
          val result: Link = retrieveHistoricFhlUkPropertyAnnualSubmission(mockAppConfig, nino = nino, taxYear = taxYear, true)

          result shouldBe
            Link("/individuals/business/property/uk/annual/furnished-holiday-lettings/{nino}/{taxYear}", GET, "self")
        }

        "return the HATEOAS link for create + amend" in new Test {
          val result: Link = createAmendHistoricFhlUkPropertyAnnualSubmission(mockAppConfig, nino = nino, taxYear = taxYear)

          result shouldBe Link(
            "/individuals/business/property/uk/annual/furnished-holiday-lettings/{nino}/{taxYear}",
            PUT,
            "create-and-amend-uk-property-historic-fhl-annual-submission"
          )
        }

        "return the HATEOAS link for delete" in new Test {
          val result: Link = deleteHistoricFhlUkPropertyAnnualSubmission(mockAppConfig, nino = nino, taxYear = taxYear)

          result shouldBe Link(
            "/individuals/business/property/uk/annual/furnished-holiday-lettings/{nino}/{taxYear}",
            DELETE,
            "delete-uk-property-historic-fhl-annual-submission"
          )
        }
      }

      "non-fhl" must {
        "return the HATEOAS link for retrieve" in new Test {
          val result: Link = retrieveHistoricNonFhlUkPropertyAnnualSubmission(mockAppConfig, nino = nino, taxYear = taxYear, true)

          result shouldBe
            Link("/individuals/business/property/uk/annual/non-furnished-holiday-lettings/{nino}/{taxYear}", GET, "self")
        }

        "return the HATEOAS link for create + amend" in new Test {
          val result: Link = createAmendHistoricNonFhlUkPropertyAnnualSubmission(mockAppConfig, nino = nino, taxYear = taxYear)

          result shouldBe Link(
            "/individuals/business/property/uk/annual/non-furnished-holiday-lettings/{nino}/{taxYear}",
            PUT,
            "create-and-amend-uk-property-historic-non-fhl-annual-submission"
          )
        }

        "return the HATEOAS link for delete" in new Test {
          val result: Link = deleteHistoricNonFhlUkPropertyAnnualSubmission(mockAppConfig, nino = nino, taxYear = taxYear)

          result shouldBe Link(
            "/individuals/business/property/uk/annual/non-furnished-holiday-lettings/{nino}/{taxYear}",
            DELETE,
            "delete-uk-property-historic-non-fhl-annual-submission"
          )
        }
      }
    }

    "produce the correct links" when {
      "called" in {
        val data: RetrieveHistoricNonFhlUkPiePeriodSummaryHateoasData = RetrieveHistoricNonFhlUkPiePeriodSummaryHateoasData("myNino", "myPeriodId")

        MockAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()

        RetrieveHistoricNonFhlUkPiePeriodSummaryResponse.RetrieveNonFhlUkPiePeriodSummaryLinksFactory.links(mockAppConfig, data) shouldBe Seq(
          Link(
            href = s"/my/context/uk/period/non-furnished-holiday-lettings/${data.nino}/${data.periodId}",
            method = Method.PUT,
            rel = "amend-uk-property-historic-non-fhl-period-summary"
          ),
          Link(href = s"/my/context/uk/period/non-furnished-holiday-lettings/${data.nino}/${data.periodId}", method = Method.GET, rel = "self"),
          Link(
            href = s"/my/context/uk/period/non-furnished-holiday-lettings/${data.nino}",
            method = Method.GET,
            rel = "list-uk-property-historic-non-fhl-period-summaries")
        )
      }
    }
  }

}
