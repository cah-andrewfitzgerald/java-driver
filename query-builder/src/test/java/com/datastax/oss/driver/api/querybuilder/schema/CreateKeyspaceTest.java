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
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilderDsl.createKeyspace;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

public class CreateKeyspaceTest {

  @Test
  public void should_create_keyspace_with_no_options() {
    assertThat(createKeyspace("foo")).hasCql("CREATE KEYSPACE foo");
  }

  @Test
  public void should_create_keyspace_if_not_exists() {
    assertThat(createKeyspace("foo").ifNotExists()).hasCql("CREATE KEYSPACE IF NOT EXISTS foo");
  }

  @Test
  public void should_create_keyspace_simple_strategy() {
    assertThat(createKeyspace("foo").withSimpleStrategy(5))
        .hasCql(
            "CREATE KEYSPACE foo WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 5 }");
  }

  @Test
  public void should_create_keyspace_simple_strategy_and_durable_writes() {
    assertThat(createKeyspace("foo").withSimpleStrategy(5).withDurableWrites(true))
        .hasCql(
            "CREATE KEYSPACE foo WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 5 } AND durable_writes = true");
  }

  @Test
  public void should_create_keyspace_network_topology_strategy() {
    assertThat(
            createKeyspace("foo").withNetworkTopologyStrategy(ImmutableMap.of("dc1", 3, "dc2", 4)))
        .hasCql(
            "CREATE KEYSPACE foo WITH replication = { 'class' : 'NetworkTopologyStrategy', 'dc1' : 3, 'dc2' : 4 }");
  }

  @Test
  public void should_create_keyspace_with_custom_properties() {
    assertThat(
            createKeyspace("foo")
                .withProperty("awesome_feature", true)
                .withProperty("wow_factor", 11)
                .withProperty("random_string", "hi"))
        .hasCql(
            "CREATE KEYSPACE foo WITH awesome_feature = true AND wow_factor = 11 AND random_string = 'hi'");
  }
}