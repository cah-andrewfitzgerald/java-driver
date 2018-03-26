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

import com.datastax.oss.driver.api.querybuilder.BuildableQuery;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public interface CreateKeyspace extends BuildableQuery, PropertyHolder<CreateKeyspace> {

  /**
   * Adds SimpleStrategy replication options with the given replication factor.
   *
   * <p>Note that using this will overwrite any previous use of this method or {@link
   * #withNetworkTopologyStrategy(Map)}.
   */
  default CreateKeyspace withSimpleStrategy(int replicationFactor) {
    ImmutableMap<String, Object> replication =
        ImmutableMap.<String, Object>builder()
            .put("class", "SimpleStrategy")
            .put("replication_factor", replicationFactor)
            .build();

    return withProperty("replication", replication);
  }

  /**
   * Adds NetworkTopologyStrategy replication options with the given data center replication
   * factors.
   *
   * <p>Note that using this will overwrite any previous use of this method or {@link
   * #withSimpleStrategy(int)}.
   *
   * @param replications Mapping of data center name to replication factor to use for that data
   *     center.
   */
  default CreateKeyspace withNetworkTopologyStrategy(Map<String, Integer> replications) {
    ImmutableMap.Builder<String, Object> replicationBuilder =
        ImmutableMap.<String, Object>builder().put("class", "NetworkTopologyStrategy");

    for (Map.Entry<String, Integer> replication : replications.entrySet()) {
      replicationBuilder.put(replication.getKey(), replication.getValue());
    }

    return withProperty("replication", replicationBuilder.build());
  }

  /**
   * Adds durable writes configuration to this keyspace. If set to false, data written to the
   * keyspace will bypass the commit log.
   */
  default CreateKeyspace withDurableWrites(boolean durableWrites) {
    return withProperty("durable_writes", true);
  }

  /**
   * Adds 'IF NOT EXISTS" to the create keyspace specification. This indicates that the keyspace
   * should not be created it already exists.
   */
  CreateKeyspace ifNotExists();
}
