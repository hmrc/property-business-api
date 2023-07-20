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

package config

import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Configuration
import support.UnitSpec

class FeatureSwitchesSpec extends UnitSpec with ScalaCheckPropertyChecks {

  "FeatureSwitches" should {
    "return true" when {
      "the feature switch is set to true" in {
        val config = Configuration(
          "passDeleteIntentHeader.enabled"       -> true,
          "removeLossesBroughtForwardV1.enabled" -> true,
          "removeLossesBroughtForwardV2.enabled" -> true
        )
        val featureSwitches = FeatureSwitches(config)
        featureSwitches.isPassDeleteIntentEnabled shouldBe true
        featureSwitches.isRemoveLossesBroughtForwardEnabledV1 shouldBe true
        featureSwitches.isRemoveLossesBroughtForwardEnabledV2 shouldBe true
      }
    }
    "return false" when {
      "the feature switch is set to false" in {
        val config = Configuration(
          "passDeleteIntentHeader.enabled"       -> false,
          "removeLossesBroughtForwardV1.enabled" -> false,
          "removeLossesBroughtForwardV2.enabled" -> false
        )
        val featureSwitches = FeatureSwitches(config)
        featureSwitches.isPassDeleteIntentEnabled shouldBe false
        featureSwitches.isRemoveLossesBroughtForwardEnabledV1 shouldBe false
        featureSwitches.isRemoveLossesBroughtForwardEnabledV2 shouldBe false
      }
      "the feature switch is not present in the config" in {
        val config          = Configuration.empty
        val featureSwitches = FeatureSwitches(config)
        featureSwitches.isPassDeleteIntentEnabled shouldBe true
        featureSwitches.isRemoveLossesBroughtForwardEnabledV1 shouldBe true
        featureSwitches.isRemoveLossesBroughtForwardEnabledV2 shouldBe true
      }
    }
  }

}
