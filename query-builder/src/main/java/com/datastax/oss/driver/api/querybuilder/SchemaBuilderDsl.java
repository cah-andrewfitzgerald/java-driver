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

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.schema.CreateKeyspace;
import com.datastax.oss.driver.internal.querybuilder.schema.DefaultCreateKeyspace;

/** A Domain-Specific Language to build CQL DDL queries using Java code. */
public class SchemaBuilderDsl {

  /** Starts a CREATE KEYSPACE query. */
  public static CreateKeyspace createKeyspace(CqlIdentifier keyspaceName) {
    return new DefaultCreateKeyspace(keyspaceName);
  }

  /**
   * Shortcut for {@link #createKeyspace(CqlIdentifier)
   * createKeyspace(CqlIdentifier.fromCql(keyspaceName))}
   */
  public static CreateKeyspace createKeyspace(String keyspaceName) {
    return createKeyspace(CqlIdentifier.fromCql(keyspaceName));
  }
}
