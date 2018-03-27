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
package com.datastax.oss.driver.api.querybuilder.schema.compaction;

public interface SizeTieredCompactionStrategy<SelfT extends SizeTieredCompactionStrategy<SelfT>>
    extends CompactionStrategy<SelfT> {

  default SelfT withMaxThreshold(int maxThreshold) {
    return withProperty("max_threshold", maxThreshold);
  }

  default SelfT withMinThreshold(int minThreshold) {
    return withProperty("min_threshold", minThreshold);
  }

  default SelfT withMinSSTableSizeInBytes(long bytes) {
    return withProperty("min_sstable_size", bytes);
  }

  default SelfT withOnlyPurgeRepairedTombstones(boolean enabled) {
    return withProperty("only_purge_repaired_tombstones", enabled);
  }

  default SelfT withBucketHigh(double bucketHigh) {
    return withProperty("bucket_high", bucketHigh);
  }

  default SelfT withBucketLow(double bucketHigh) {
    return withProperty("bucket_low", bucketHigh);
  }

  // 2.1 only
  default SelfT withColdReadsToOmit(double ratio) {
    return withProperty("cold_reads_to_omit", ratio);
  }
}
