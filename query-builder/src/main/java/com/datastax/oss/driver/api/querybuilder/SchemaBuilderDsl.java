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
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.querybuilder.schema.CreateKeyspace;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.datastax.oss.driver.api.querybuilder.schema.compaction.LeveledCompactionStrategy;
import com.datastax.oss.driver.api.querybuilder.schema.compaction.SizeTieredCompactionStrategy;
import com.datastax.oss.driver.api.querybuilder.schema.compaction.TimeWindowCompactionStrategy;
import com.datastax.oss.driver.internal.core.metadata.schema.ShallowUserDefinedType;
import com.datastax.oss.driver.internal.querybuilder.schema.DefaultCreateKeyspace;
import com.datastax.oss.driver.internal.querybuilder.schema.DefaultCreateTable;
import com.datastax.oss.driver.internal.querybuilder.schema.compaction.DefaultLeveledCompactionStrategy;
import com.datastax.oss.driver.internal.querybuilder.schema.compaction.DefaultSizeTieredCompactionStrategy;
import com.datastax.oss.driver.internal.querybuilder.schema.compaction.DefaultTimeWindowCompactionStrategy;

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

  public static CreateTable createTable(CqlIdentifier tableName) {
    return new DefaultCreateTable(tableName);
  }

  public static CreateTable createTable(CqlIdentifier keyspace, CqlIdentifier tableName) {
    return new DefaultCreateTable(keyspace, tableName);
  }

  public static CreateTable createTable(String tableName) {
    return createTable(CqlIdentifier.fromCql(tableName));
  }

  public static CreateTable createTable(String keyspace, String tableName) {
    return createTable(CqlIdentifier.fromCql(keyspace), CqlIdentifier.fromCql(tableName));
  }

  public static SizeTieredCompactionStrategy sizeTieredCompactionStrategy() {
    return new DefaultSizeTieredCompactionStrategy();
  }

  public static LeveledCompactionStrategy leveledCompactionStrategy() {
    return new DefaultLeveledCompactionStrategy();
  }

  public static TimeWindowCompactionStrategy timeWindowCompactionStrategy() {
    return new DefaultTimeWindowCompactionStrategy();
  }

  // Short cuts for getting a DataType reference for UDTs.

  public static UserDefinedType udt(CqlIdentifier keyspace, CqlIdentifier name, boolean frozen) {
    return new ShallowUserDefinedType(keyspace, name, frozen);
  }

  public static UserDefinedType udt(String keyspace, String name, boolean frozen) {
    return udt(CqlIdentifier.fromCql(keyspace), CqlIdentifier.fromCql(name), frozen);
  }

  public static UserDefinedType udt(CqlIdentifier name, boolean frozen) {
    return new ShallowUserDefinedType(null, name, frozen);
  }

  public static UserDefinedType udt(String name, boolean frozen) {
    return udt(CqlIdentifier.fromCql(name), frozen);
  }

  public enum KeyCaching {
    ALL,
    NONE;
  }

  public static class RowsPerPartition {

    private final String value;

    private RowsPerPartition(String value) {
      this.value = value;
    }

    public static RowsPerPartition ALL = new RowsPerPartition("ALL");

    public static RowsPerPartition NONE = new RowsPerPartition("NONE");

    public static RowsPerPartition rows(int rowNumber) {
      return new RowsPerPartition(Integer.toString(rowNumber));
    }

    public String getValue() {
      return value;
    }
  }
}
