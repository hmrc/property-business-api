/*
 * Copyright 2025 HM Revenue & Customs
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

package common.controllers.validators.resolvers

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import common.models.domain.PropertyId
import common.models.errors.PropertyIdFormatError
import shared.models.errors.MtdError
import shared.utils.UnitSpec

import java.util.UUID

class ResolveUuidSpec extends UnitSpec {

  "ResolveUuid" should {
    "return no errors" when {
      (1 to 9).foreach { version =>
        val uuid: String = s"4557ecb5-fd32-${version}8cc-81f5-e6acd1099f3c"

        s"given a valid UUID v$version string" in {
          assert(UUID.fromString(uuid).version() == version)

          val result: Validated[Seq[MtdError], PropertyId] = ResolveUuid(uuid, PropertyIdFormatError)(PropertyId.apply)
          result shouldBe Valid(PropertyId(uuid))
        }

        s"given an optional valid UUID v$version string" in {
          assert(UUID.fromString(uuid).version() == version)

          val result: Validated[Seq[MtdError], Option[PropertyId]] = ResolveUuid(Some(uuid), PropertyIdFormatError)(PropertyId.apply)
          result shouldBe Valid(Some(PropertyId(uuid)))
        }
      }

      "given no UUID (None)" in {
        val result: Validated[Seq[MtdError], Option[PropertyId]] = ResolveUuid(None, PropertyIdFormatError)(PropertyId.apply)
        result shouldBe Valid(None)
      }
    }

    "return the expected error" when {
      val invalidUuid: String = "not-a-uuid"

      "given a non-UUID string" in {
        val result: Validated[Seq[MtdError], PropertyId] = ResolveUuid(invalidUuid, PropertyIdFormatError)(PropertyId.apply)

        result shouldBe Invalid(List(PropertyIdFormatError))
      }

      "given an optional non-UUID string" in {
        val result: Validated[Seq[MtdError], Option[PropertyId]] = ResolveUuid(Some(invalidUuid), PropertyIdFormatError)(PropertyId.apply)

        result shouldBe Invalid(List(PropertyIdFormatError))
      }
    }
  }

}
