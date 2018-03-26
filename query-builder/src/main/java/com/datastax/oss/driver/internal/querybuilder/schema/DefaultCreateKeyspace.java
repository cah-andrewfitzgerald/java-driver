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
package com.datastax.oss.driver.internal.querybuilder.schema;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.schema.CreateKeyspace;
import com.datastax.oss.driver.internal.querybuilder.ImmutableCollections;
import com.google.common.collect.ImmutableMap;

public class DefaultCreateKeyspace implements CreateKeyspace {

  private final CqlIdentifier keyspaceName;
  private final boolean ifNotExists;
  private final ImmutableMap<String, Object> properties;

  public DefaultCreateKeyspace(CqlIdentifier keyspaceName) {
    this(keyspaceName, false, ImmutableMap.of());
  }

  public DefaultCreateKeyspace(
      CqlIdentifier keyspaceName, boolean ifNotExists, ImmutableMap<String, Object> properties) {
    this.keyspaceName = keyspaceName;
    this.ifNotExists = ifNotExists;
    this.properties = properties;
  }

  @Override
  public CreateKeyspace withProperty(String name, Object value) {
    return new DefaultCreateKeyspace(
        keyspaceName, ifNotExists, ImmutableCollections.append(properties, name, value));
  }

  @Override
  public CreateKeyspace ifNotExists() {
    return new DefaultCreateKeyspace(keyspaceName, true, properties);
  }

  @Override
  public String asCql() {
    StringBuilder builder = new StringBuilder();

    builder.append("CREATE KEYSPACE ");
    if (ifNotExists) {
      builder.append("IF NOT EXISTS ");
    }

    builder.append(keyspaceName.asCql(true));
    builder.append(PropertyUtils.buildProperties(properties, true));
    return builder.toString();
  }
}
