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

package config

import com.typesafe.config.ConfigFactory
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Configuration
import support.UnitSpec

class FeatureSwitchSpec extends UnitSpec with ScalaCheckPropertyChecks {

  private def featureSwitch(optConfigString: Option[String]) =
    FeatureSwitch(optConfigString.map(configString => Configuration(ConfigFactory.parseString(configString))))

  "v2r7c feature switch" when {
    "present and configured" must {
      "return the configured value" in forAll { enabled : Boolean =>
        featureSwitch(Some(s"v2r7c-endpoints.enabled = $enabled")).isV2R7cRoutingEnabled shouldBe enabled
      }
    }

    "v1rc switch is absent" must {
      "default to true" in {
        featureSwitch(Some("somethingElse = someValue")).isV2R7cRoutingEnabled shouldBe true
      }
    }

    "feature switch config is completely absent" must {
      "default to true" in {
        featureSwitch(None).isV2R7cRoutingEnabled shouldBe true
      }
    }
  }
}
