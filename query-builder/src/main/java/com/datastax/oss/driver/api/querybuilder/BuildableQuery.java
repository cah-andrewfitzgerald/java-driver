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

import com.datastax.oss.driver.api.core.cql.SimpleStatement;

/**
 * End state for the query builder DSL.
 *
 * <p>The API returns this type as soon as there is enough information to build a query (in most
 * cases, it's still possible to call more methods to keep building the query).
 *
 * <p>The resulting {@link SimpleStatement} can be executed directly (possibly after adding
 * parameters) or prepared.
 */
public interface BuildableQuery {
  // TODO do we really need pretty-printing?
  SimpleStatement build(boolean pretty);
}
