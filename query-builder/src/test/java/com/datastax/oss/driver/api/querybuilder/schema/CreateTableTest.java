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

import static com.datastax.oss.driver.api.querybuilder.Assertions.assertThat;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilderDsl.createTable;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilderDsl.udt;

import com.datastax.oss.driver.api.core.type.DataTypes;
import org.junit.Test;

public class CreateTableTest {

  @Test
  public void should_create_table_with_single_partition_key() {
    assertThat(
            createTable("bar").withPartitionKey("k", DataTypes.INT).withColumn("v", DataTypes.TEXT))
        .hasCql("CREATE TABLE bar (k int PRIMARY KEY,v text)");
  }

  @Test
  public void should_create_table_with_compound_partition_key() {
    assertThat(
            createTable("bar")
                .withPartitionKey("kc", DataTypes.INT)
                .withPartitionKey("ka", DataTypes.TIMESTAMP)
                .withColumn("v", DataTypes.TEXT))
        .hasCql("CREATE TABLE bar (kc int,ka timestamp,v text,PRIMARY KEY((kc,ka)))");
  }

  @Test
  public void should_create_table_with_single_partition_key_and_clustering_column() {
    assertThat(
            createTable("bar")
                .withPartitionKey("k", DataTypes.INT)
                .withClusteringColumn("c", udt("category", true))
                .withColumn("v", DataTypes.TEXT))
        .hasCql("CREATE TABLE bar (k int,c frozen<category>,v text,PRIMARY KEY(k,c))");
  }

  @Test
  public void should_create_table_with_compound_partition_key_and_clustering_columns() {
    assertThat(
            createTable("bar")
                .withPartitionKey("kc", DataTypes.INT)
                .withPartitionKey("ka", DataTypes.TIMESTAMP)
                .withClusteringColumn("c", DataTypes.FLOAT)
                .withClusteringColumn("a", DataTypes.UUID)
                .withColumn("v", DataTypes.TEXT))
        .hasCql(
            "CREATE TABLE bar (kc int,ka timestamp,c float,a uuid,v text,PRIMARY KEY((kc,ka),c,a))");
  }

  @Test
  public void should_create_table_with_compact_storage() {
    assertThat(
            createTable("bar")
                .withPartitionKey("k", DataTypes.INT)
                .withColumn("v", DataTypes.TEXT)
                .withCompactStorage())
        .hasCql("CREATE TABLE bar (k int PRIMARY KEY,v text) WITH COMPACT STORAGE");
  }

  @Test
  public void should_create_table_with_compact_storage_and_default_ttl() {
    assertThat(
            createTable("bar")
                .withPartitionKey("k", DataTypes.INT)
                .withColumn("v", DataTypes.TEXT)
                .withCompactStorage()
                .withDefaultTimeToLiveSeconds(86400))
        .hasCql(
            "CREATE TABLE bar (k int PRIMARY KEY,v text) WITH COMPACT STORAGE AND default_time_to_live=86400");
  }

  @Test
  public void should_create_table_with_options() {
    assertThat(
            createTable("bar")
                .withPartitionKey("k", DataTypes.INT)
                .withColumn("v", DataTypes.TEXT)
                .withBloomFilterFpChance(0.42)
                .withCDC(false)
                .withComment("Hello world")
                .withDcLocalReadRepairChance(0.54)
                .withDefaultTimeToLiveSeconds(86400)
                .withGcGraceSeconds(864000)
                .withMemtableFlushPeriodInMs(10000)
                .withMinIndexInterval(1024)
                .withMaxIndexInterval(4096)
                .withReadRepairChance(0.55)
                .withSpeculativeRetry("99percentile"))
        .hasCql(
            "CREATE TABLE bar (k int PRIMARY KEY,v text) WITH bloom_filter_fp_chance=0.42 AND cdc=false AND comment='Hello world' AND dclocal_read_repair_chance=0.54 AND default_time_to_live=86400 AND gc_grace_seconds=864000 AND memtable_flush_period_in_ms=10000 AND min_index_interval=1024 AND max_index_interval=4096 AND read_repair_chance=0.55 AND speculative_retry='99percentile'");
  }
}
