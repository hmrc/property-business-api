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

package v2.hateoas

import mocks.MockAppConfig
import support.UnitSpec
import v2.models.hateoas.Link
import v2.models.hateoas.Method._

class HateoasLinksSpec extends UnitSpec with MockAppConfig with HateoasLinks {

  val nino         = "{nino}"
  val businessId   = "{businessId}"
  val taxYear      = "{taxYear}"
  val submissionId = "{submissionId}"
  val periodId     = "{periodId}"

  class Test {
    MockAppConfig.apiGatewayContext returns "individuals/business/property" anyNumberOfTimes ()
  }

  "Hateoas links" when {
    "for foreign property endpoints" must {
      "work for FP1 'Create FP period summary'" in new Test {
        createForeignPropertyPeriodSummary(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear) shouldBe
          Link("/individuals/business/property/foreign/{nino}/{businessId}/period/{taxYear}", POST, "create-foreign-property-period-summary")
      }

      "work for FP3 'Amend FP Period Summary'" in new Test {
        amendForeignPropertyPeriodSummary(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear, submissionId = submissionId) shouldBe
          Link("/individuals/business/property/foreign/{nino}/{businessId}/period/{taxYear}/{submissionId}",
               PUT,
               "amend-foreign-property-period-summary")
      }

      "work for FP4 'Retrieve FP Period Summary'" in new Test {
        retrieveForeignPropertyPeriodSummary(mockAppConfig,
                                             nino = nino,
                                             businessId = businessId,
                                             taxYear = taxYear,
                                             submissionId = submissionId,
                                             self = true) shouldBe
          Link("/individuals/business/property/foreign/{nino}/{businessId}/period/{taxYear}/{submissionId}", GET, "self")

        retrieveForeignPropertyPeriodSummary(mockAppConfig,
                                             nino = nino,
                                             businessId = businessId,
                                             taxYear = taxYear,
                                             submissionId = submissionId,
                                             self = false) shouldBe
          Link("/individuals/business/property/foreign/{nino}/{businessId}/period/{taxYear}/{submissionId}",
               GET,
               "retrieve-foreign-property-period-summary")
      }

      "work for FP5 'Create and Amend FP Annual Submission'" in new Test {
        createAmendForeignPropertyAnnualSubmission(mockAppConfig, nino = nino, businessId = businessId, taxYear = taxYear) shouldBe
          Link("/individuals/business/property/foreign/{nino}/{businessId}/annual/{taxYear}",
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
        retrieveUkPropertyPeriodSummary(mockAppConfig,
                                        nino = nino,
                                        businessId = businessId,
                                        taxYear = taxYear,
                                        submissionId = submissionId,
                                        self = true) shouldBe
          Link("/individuals/business/property/uk/{nino}/{businessId}/period/{taxYear}/{submissionId}", GET, "self")

        retrieveUkPropertyPeriodSummary(mockAppConfig,
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

    "for Historic Uk Property Income & Expenses (PIE) Period Summary" must {
      "work for PIE1 'Retrieve a Period Summary'" in new Test {
        retrieveHistoricFhlUkPiePeriodSummary(mockAppConfig, nino = nino, periodId = periodId) shouldBe
          Link(
            "/individuals/business/property/uk/furnished-holiday-lettings/{nino}/{periodId}",
            GET,
            "retrieve-historic-uk-fhl-property-income-expenses-period-summary"
          )
      }

      "work for PIE2 'Amend a Period Summary'" in new Test {
        amendHistoricFhlUkPiePeriodSummary(mockAppConfig, nino = nino, periodId = periodId) shouldBe
          Link("/individuals/business/property/uk/furnished-holiday-lettings/{nino}/{periodId}",
               PUT,
               "amend-historic-uk-fhl-property-income-expenses-period-summary")
      }
    }
  }
}
