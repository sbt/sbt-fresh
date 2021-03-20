/*
 * Copyright 2016 Heiko Seeberger
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

package de.heikoseeberger.sbtfresh

final case class License(id: String, name: String, url: String)

object License {

  // Attention: headerName must match objects implementing `License` in sbt-header!

  final val apache20: License =
    License("apache20", "Apache-2.0", "http://www.apache.org/licenses/LICENSE-2.0")

  final val agpl3: License =
    License("agpl3", "AGPL-3.0", "https://www.gnu.org/licenses/agpl.html")

  final val bsd2: License =
    License("bsd2", "BSD-2-Clause", "https://opensource.org/licenses/BSD-2-Clause")

  final val bsd3: License =
    License("bsd3", "BSD-3-Clause", "https://opensource.org/licenses/BSD-3-Clause")

  final val gpl3: License =
    License("gpl3", "GPL-3.0", "http://www.gnu.org/licenses/gpl-3.0.en.html")

  final val lgpl3: License =
    License("lgpl3", "LGPL-3.0", "https://www.gnu.org/licenses/lgpl-3.0.en.html")

  final val mit: License =
    License("mit", "MIT", "https://opensource.org/licenses/MIT")

  def values: Set[License] =
    Set(apache20, agpl3, bsd2, bsd3, gpl3, lgpl3, mit)
}
