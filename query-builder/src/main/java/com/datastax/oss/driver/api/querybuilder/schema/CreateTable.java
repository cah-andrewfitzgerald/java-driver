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
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.querybuilder.BuildableQuery;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public interface CreateTable extends BuildableQuery, RelationStructure<CreateTable> {

  CreateTable ifNotExists();

  CreateTable withPartitionKey(CqlIdentifier columnName, DataType dataType);

  default CreateTable withPartitionKey(String columnName, DataType dataType) {
    return withPartitionKey(CqlIdentifier.fromCql(columnName), dataType);
  }

  CreateTable withClusteringColumn(CqlIdentifier columnName, DataType dataType);

  default CreateTable withClusteringColumn(String columnName, DataType dataType) {
    return withClusteringColumn(CqlIdentifier.fromCql(columnName), dataType);
  }

  CreateTable withColumn(CqlIdentifier columnName, DataType dataType);

  default CreateTable withColumn(String columnName, DataType dataType) {
    return withColumn(CqlIdentifier.fromCql(columnName), dataType);
  }

  CreateTable withStaticColumn(CqlIdentifier columnName, DataType dataType);

  default CreateTable withStaticColumn(String columnName, DataType dataType) {
    return withStaticColumn(CqlIdentifier.fromCql(columnName), dataType);
  }

  CreateTable withCompactStorage();

  CreateTable withClusteringOrderByIds(Map<CqlIdentifier, ClusteringOrder> orderings);

  default CreateTable withClusteringOrder(Map<String, ClusteringOrder> orderings) {
    ImmutableMap.Builder<CqlIdentifier, ClusteringOrder> builder = ImmutableMap.builder();
    for (Map.Entry<String, ClusteringOrder> entry : orderings.entrySet()) {
      builder.put(CqlIdentifier.fromCql(entry.getKey()), entry.getValue());
    }
    // build() throws if there are duplicate keys
    return withClusteringOrderByIds(builder.build());
  }

  CreateTable withClusteringOrder(CqlIdentifier columnName, ClusteringOrder order);

  default CreateTable withClusteringOrder(String columnName, ClusteringOrder order) {
    return withClusteringOrder(CqlIdentifier.fromCql(columnName), order);
  }
}
