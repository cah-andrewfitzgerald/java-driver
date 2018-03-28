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
package com.datastax.oss.driver.api.querybuilder.schema;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.querybuilder.BuildableQuery;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public interface CreateTableWithOptions
    extends BuildableQuery, RelationStructure<CreateTableWithOptions> {
  CreateTableWithOptions withCompactStorage();

  CreateTableWithOptions withClusteringOrderByIds(Map<CqlIdentifier, ClusteringOrder> orderings);

  default CreateTableWithOptions withClusteringOrder(Map<String, ClusteringOrder> orderings) {
    ImmutableMap.Builder<CqlIdentifier, ClusteringOrder> builder = ImmutableMap.builder();
    for (Map.Entry<String, ClusteringOrder> entry : orderings.entrySet()) {
      builder.put(CqlIdentifier.fromCql(entry.getKey()), entry.getValue());
    }
    // build() throws if there are duplicate keys
    return withClusteringOrderByIds(builder.build());
  }

  CreateTableWithOptions withClusteringOrder(CqlIdentifier columnName, ClusteringOrder order);

  default CreateTableWithOptions withClusteringOrder(String columnName, ClusteringOrder order) {
    return withClusteringOrder(CqlIdentifier.fromCql(columnName), order);
  }
}
