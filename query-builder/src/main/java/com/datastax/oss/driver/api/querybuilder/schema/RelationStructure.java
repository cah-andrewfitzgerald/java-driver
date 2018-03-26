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

public interface RelationStructure<SelfT extends RelationStructure<SelfT>>
    extends PropertyHolder<SelfT> {

  default SelfT withBloomFilterFpChance(double bloomFilterFpChance) {
    return withProperty("bloom_filter_fp_chance", bloomFilterFpChance);
  }

  default SelfT withCDC(boolean enabled) {
    return withProperty("cdc", enabled);
  }

  default SelfT withComment(String comment) {
    return withProperty("comment", comment);
  }

  default SelfT withDcLocalReadRepairChance(double dcLocalReadRepairChance) {
    return withProperty("dclocal_read_repair_chance", dcLocalReadRepairChance);
  }

  default SelfT withDefaultTimeToLiveSeconds(int ttl) {
    return withProperty("default_time_to_live", ttl);
  }

  default SelfT withGcGraceSeconds(int gcGraceSeconds) {
    return withProperty("gc_grace_seconds", gcGraceSeconds);
  }

  default SelfT withMemtableFlushPeriodInMs(int memtableFlushPeriodInMs) {
    return withProperty("memtable_flush_period_in_ms", memtableFlushPeriodInMs);
  }

  default SelfT withMinIndexInterval(int min) {
    return withProperty("min_index_interval", min);
  }

  default SelfT withMaxIndexInterval(int max) {
    return withProperty("max_index_interval", max);
  }

  default SelfT withReadRepairChance(double readRepairChance) {
    return withProperty("read_repair_chance", readRepairChance);
  }

  default SelfT withSpeculativeRetry(String speculativeRetry) {
    return withProperty("speculative_retry", speculativeRetry);
  }

  // TODO
  // compaction
  // compression
  // caching
}
