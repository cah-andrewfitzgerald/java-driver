/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.api.querybuilder;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;

public class BuildableQueryAssert extends AbstractAssert<BuildableQueryAssert, BuildableQuery> {

  public BuildableQueryAssert(BuildableQuery actual) {
    super(actual, BuildableQueryAssert.class);
  }

  public BuildableQueryAssert hasPrettyCql(String... expectedLines) {
    StringBuilder expectedCql = new StringBuilder();
    for (int i = 0; i < expectedLines.length; i++) {
      if (i > 0) {
        expectedCql.append('\n');
      }
      expectedCql.append(expectedLines[i]);
    }
    assertThat(actual.build(true).getQuery()).isEqualTo(expectedCql.toString());
    return this;
  }

  public BuildableQueryAssert hasUglyCql(String expected) {
    assertThat(actual.build(false).getQuery()).isEqualTo(expected);
    return this;
  }
}
