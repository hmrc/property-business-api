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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.{CountryCodeFormatError, RuleCountryCodeError}

class CountryCodeValidationSpec extends UnitSpec {
  "CountryCodeValidation" when {
    "validate" must {
      Seq("AFG", "ALB", "DZA", "ASM", "AND", "AGO", "AIA", "ATG", "ARG", "ARM", "ABW", "AUS", "AUT", "AZE", "BHS",
        "BHR", "BGD", "BRB", "BLR", "BEL", "BLZ", "BEN", "BMU", "BTN", "BOL", "BES", "BIH", "BWA", "BRA", "VGB",
        "BRN", "BGR", "BFA", "MMR", "BDI", "KHM", "CMR", "CAN", "CPV", "CYM", "CAF", "TCD", "CHL", "CHN", "CXR",
        "CCK", "COL", "COM", "COG", "COK", "CRI", "CIV", "HRV", "CUB", "CUW", "CYP", "CZE", "COD", "DNK", "DJI",
        "DMA", "DOM", "ECU", "EGY", "SLV", "GNQ", "ERI", "EST", "ETH", "FLK", "FRO", "FJI", "FIN", "FRA", "GUF",
        "PYF", "GAB", "GMB", "GEO", "DEU", "GHA", "GIB", "GRC", "GRL", "GRD", "GLP", "GUM", "GTM", "GGY", "GIN",
        "GNB", "GUY", "HTI", "HND", "HKG", "HUN", "ISL", "IND", "IDN", "IRN", "IRQ", "IRL", "IMN", "ISR", "ITA",
        "JAM", "JPN", "JEY", "JOR", "KAZ", "KEN", "KIR", "XKX", "KWT", "KGZ", "LAO", "LVA", "LBN", "LSO", "LBR",
        "LBY", "LIE", "LTU", "LUX", "MAC", "MKD", "MDG", "MWI", "MYS", "MDV", "MLI", "MLT", "MHL", "MTQ", "MRT",
        "MUS", "MYT", "MEX", "FSM", "MDA", "MCO", "MNG", "MNE", "MSR", "MAR", "MOZ", "NAM", "NRU", "NPL", "NLD",
        "NCL", "NZL", "NIC", "NER", "NGA", "NIU", "NFK", "PRK", "MNP", "NOR", "OMN", "PAK", "PLW", "PAN", "PNG",
        "PRY", "PER", "PHL", "PCN", "POL", "PRT", "PRI", "QAT", "REU", "ROU", "RUS", "RWA", "SHN", "KNA", "LCA",
        "SPM", "VCT", "WSM", "SMR", "STP", "SAU", "SEN", "SRB", "SYC", "SLE", "SGP", "SXM", "SVK", "SVN",
        "SLB", "SOM", "ZAF", "KOR", "SSD", "ESP", "LKA", "SDN", "SUR", "SJM", "SWZ", "SWE", "CHE", "SYR", "TWN",
        "TJK", "TZA", "THA", "TLS", "TGO", "TKL", "TON", "TTO", "TUN", "TUR", "TKM", "TCA", "TUV", "UGA", "UKR",
        "ARE", "USA", "VIR", "URY", "UZB", "VUT", "VAT", "VEN", "VNM", "WLF", "YEM", "ZMB", "ZWE").foreach {
        code =>
          s"return an empty list for valid country code $code" in {
            CountryCodeValidation.validate(code, "path") shouldBe NoValidationErrors
          }
      }

      "return a CountryCodeFormatError for an invalid country code" in {
        CountryCodeValidation.validate("notACountryCode", "path") shouldBe List(CountryCodeFormatError.copy(paths = Some(Seq("path"))))
      }

      "return a CountryCodeFormatError for an invalid format country code" in {
        CountryCodeValidation.validate("FRANCE", "path") shouldBe List(CountryCodeFormatError.copy(paths = Some(Seq("path"))))
      }

      "return a CountryCodeFormatError for an invalid rule country code" in {
        CountryCodeValidation.validate("FRE", "path") shouldBe List(RuleCountryCodeError.copy(paths = Some(Seq("path"))))
      }
    }
  }
}