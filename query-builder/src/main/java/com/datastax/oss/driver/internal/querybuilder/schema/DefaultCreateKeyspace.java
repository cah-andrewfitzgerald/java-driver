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
import java.util.Map;

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

  @SuppressWarnings("unchecked")
  private String extractPropertyValue(Object property) {
    StringBuilder propertyValue = new StringBuilder();
    if (property instanceof String) {
      propertyValue.append("'").append((String) property).append("'");
    } else if (property instanceof Map) {
      Map<String, Object> propertyMap = (Map<String, Object>) property;
      boolean first = true;
      propertyValue.append("{ ");
      for (Map.Entry<String, Object> subProperty : propertyMap.entrySet()) {
        if (first) {
          first = false;
        } else {
          propertyValue.append(", ");
        }
        propertyValue
            .append("'")
            .append(subProperty.getKey())
            .append("' : ")
            .append(extractPropertyValue(subProperty.getValue()));
      }
      propertyValue.append(" }");
      // parse
    } else {
      propertyValue.append(property);
    }
    return propertyValue.toString();
  }

  @Override
  public String asCql() {
    StringBuilder builder = new StringBuilder();

    builder.append("CREATE KEYSPACE ");
    if (ifNotExists) {
      builder.append("IF NOT EXISTS ");
    }

    builder.append(keyspaceName.asCql(true));

    boolean first = true;
    for (Map.Entry<String, Object> property : properties.entrySet()) {
      if (first) {
        builder.append(" WITH ");
        first = false;
      } else {
        builder.append(" AND ");
      }
      String value = extractPropertyValue(property.getValue());
      builder.append(property.getKey()).append(" = ").append(value);
    }

    return builder.toString();
  }
}
