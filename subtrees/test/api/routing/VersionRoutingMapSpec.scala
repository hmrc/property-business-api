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

package api.routing

import api.support.UnitSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.routing.Router

class VersionRoutingMapSpec extends UnitSpec with GuiceOneAppPerSuite with ScalaCheckPropertyChecks {

  val defaultRouter: Router = mock[Router]
  val v2Router: Router      = mock[Router]
  val v3Router: Router      = mock[Router]

  val versionRouters: Map[Version, Router] = Map(
    Version2 -> v2Router,
    Version3 -> v3Router
  )

  "map" when {
    "routing to v2 and v3" should {
      val versionRoutingMap: VersionRoutingMapImpl = VersionRoutingMapImpl(
        defaultRouter = defaultRouter,
        versionRouters = versionRouters
      )

      s"route to v2Router" in {
        versionRoutingMap.map(Version2) shouldBe v2Router
      }

      s"route to v3Router" in {
        versionRoutingMap.map(Version3) shouldBe v3Router
      }
    }
  }

}
