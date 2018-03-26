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
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.datastax.oss.driver.internal.querybuilder.ImmutableCollections;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class DefaultCreateTable implements CreateTable {

  private final CqlIdentifier keyspace;
  private final CqlIdentifier tableName;

  private final boolean ifNotExists;
  private final boolean compactStorage;

  private final ImmutableMap<String, Object> properties;

  private final ImmutableMap<CqlIdentifier, DataType> columnsInOrder;

  // TODO: These can be lists.
  private final ImmutableMap<CqlIdentifier, DataType> partitionKeyColumns;
  private final ImmutableMap<CqlIdentifier, DataType> clusteringKeyColumns;
  private final ImmutableMap<CqlIdentifier, DataType> staticColumns;
  private final ImmutableMap<CqlIdentifier, DataType> regularColumns;

  public DefaultCreateTable(CqlIdentifier tableName) {
    this(null, tableName);
  }

  public DefaultCreateTable(CqlIdentifier keyspace, CqlIdentifier tableName) {
    this(
        keyspace,
        tableName,
        false,
        false,
        ImmutableMap.of(),
        ImmutableMap.of(),
        ImmutableMap.of(),
        ImmutableMap.of(),
        ImmutableMap.of(),
        ImmutableMap.of());
  }

  public DefaultCreateTable(
      CqlIdentifier keyspace,
      CqlIdentifier tableName,
      boolean ifNotExists,
      boolean compactStorage,
      ImmutableMap<CqlIdentifier, DataType> columnsInOrder,
      ImmutableMap<CqlIdentifier, DataType> partitionKeyColumns,
      ImmutableMap<CqlIdentifier, DataType> clusteringKeyColumns,
      ImmutableMap<CqlIdentifier, DataType> staticColumns,
      ImmutableMap<CqlIdentifier, DataType> regularColumns,
      ImmutableMap<String, Object> properties) {
    this.keyspace = keyspace;
    this.tableName = tableName;
    this.ifNotExists = ifNotExists;
    this.compactStorage = compactStorage;
    this.columnsInOrder = columnsInOrder;
    this.partitionKeyColumns = partitionKeyColumns;
    this.clusteringKeyColumns = clusteringKeyColumns;
    this.staticColumns = staticColumns;
    this.regularColumns = regularColumns;
    this.properties = properties;
  }

  @Override
  public CreateTable ifNotExists() {
    return new DefaultCreateTable(
        keyspace,
        tableName,
        true,
        compactStorage,
        columnsInOrder,
        partitionKeyColumns,
        clusteringKeyColumns,
        staticColumns,
        regularColumns,
        properties);
  }

  // TODO: Throw runtime error if column used twice.

  @Override
  public CreateTable withPartitionKey(CqlIdentifier columnName, DataType dataType) {
    return new DefaultCreateTable(
        keyspace,
        tableName,
        ifNotExists,
        compactStorage,
        ImmutableCollections.append(columnsInOrder, columnName, dataType),
        ImmutableCollections.append(partitionKeyColumns, columnName, dataType),
        clusteringKeyColumns,
        staticColumns,
        regularColumns,
        properties);
  }

  @Override
  public CreateTable withClusteringColumn(CqlIdentifier columnName, DataType dataType) {
    return new DefaultCreateTable(
        keyspace,
        tableName,
        ifNotExists,
        compactStorage,
        ImmutableCollections.append(columnsInOrder, columnName, dataType),
        partitionKeyColumns,
        ImmutableCollections.append(clusteringKeyColumns, columnName, dataType),
        staticColumns,
        regularColumns,
        properties);
  }

  @Override
  public CreateTable withColumn(CqlIdentifier columnName, DataType dataType) {
    return new DefaultCreateTable(
        keyspace,
        tableName,
        ifNotExists,
        compactStorage,
        ImmutableCollections.append(columnsInOrder, columnName, dataType),
        partitionKeyColumns,
        clusteringKeyColumns,
        staticColumns,
        ImmutableCollections.append(regularColumns, columnName, dataType),
        properties);
  }

  @Override
  public CreateTable withStaticColumn(CqlIdentifier columnName, DataType dataType) {
    return new DefaultCreateTable(
        keyspace,
        tableName,
        ifNotExists,
        compactStorage,
        ImmutableCollections.append(columnsInOrder, columnName, dataType),
        partitionKeyColumns,
        clusteringKeyColumns,
        ImmutableCollections.append(staticColumns, columnName, dataType),
        regularColumns,
        properties);
  }

  @Override
  public CreateTable withCompactStorage() {
    return new DefaultCreateTable(
        keyspace,
        tableName,
        ifNotExists,
        true,
        columnsInOrder,
        partitionKeyColumns,
        clusteringKeyColumns,
        staticColumns,
        regularColumns,
        properties);
  }

  @Override
  public CreateTable withClusteringOrderByIds(Map<CqlIdentifier, ClusteringOrder> orderings) {
    return null;
  }

  @Override
  public CreateTable withClusteringOrder(CqlIdentifier columnName, ClusteringOrder order) {
    return null;
  }

  @Override
  public String asCql() {
    StringBuilder builder = new StringBuilder();

    builder.append("CREATE TABLE ");
    if (ifNotExists) {
      builder.append("IF NOT EXISTS ");
    }

    if (keyspace != null) {
      builder.append(keyspace.asCql(true)).append('.');
    }
    builder.append(tableName.asCql(true));

    // TODO we need some kind of validation to ensure we have a proper primary key;

    boolean singlePrimaryKey = partitionKeyColumns.size() == 1 && clusteringKeyColumns.size() == 0;

    builder.append(" (");

    boolean first = true;
    for (Map.Entry<CqlIdentifier, DataType> column : columnsInOrder.entrySet()) {
      if (first) {
        first = false;
      } else {
        builder.append(',');
      }
      builder
          .append(column.getKey().asCql(true))
          .append(' ')
          .append(column.getValue().asCql(true, true));

      if (singlePrimaryKey && partitionKeyColumns.containsKey(column.getKey())) {
        builder.append(" PRIMARY KEY");
      } else if (staticColumns.containsKey(column.getKey())) {
        builder.append(" STATIC");
      }
    }

    if (!singlePrimaryKey) {
      builder.append(",PRIMARY KEY(");
      boolean firstKey = true;

      if (partitionKeyColumns.size() > 1) {
        builder.append('(');
      }
      for (Map.Entry<CqlIdentifier, DataType> partitionColumn : partitionKeyColumns.entrySet()) {
        if (firstKey) {
          firstKey = false;
        } else {
          builder.append(',');
        }
        builder.append(partitionColumn.getKey());
      }
      if (partitionKeyColumns.size() > 1) {
        builder.append(')');
      }

      for (Map.Entry<CqlIdentifier, DataType> clusteringColumn : clusteringKeyColumns.entrySet()) {
        builder.append(',').append(clusteringColumn.getKey());
      }
      builder.append(')');
    }

    builder.append(')');

    // TODO also consider clustering order.
    if (compactStorage || !properties.isEmpty()) {
      boolean firstOption = true;

      if (compactStorage) {
        firstOption = false;
        builder.append(" WITH COMPACT STORAGE");
      }
      builder.append(PropertyUtils.buildProperties(properties, firstOption));
    }

    return builder.toString();
  }

  @Override
  public CreateTable withProperty(String name, Object value) {
    return new DefaultCreateTable(
        keyspace,
        tableName,
        ifNotExists,
        compactStorage,
        columnsInOrder,
        partitionKeyColumns,
        clusteringKeyColumns,
        staticColumns,
        regularColumns,
        ImmutableCollections.append(properties, name, value));
  }
}
