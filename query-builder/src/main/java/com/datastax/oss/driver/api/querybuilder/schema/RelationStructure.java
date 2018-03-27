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

import com.datastax.oss.driver.api.querybuilder.SchemaBuilderDsl.KeyCaching;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilderDsl.RowsPerPartition;
import com.datastax.oss.driver.api.querybuilder.schema.compaction.CompactionStrategy;
import com.google.common.collect.ImmutableMap;

public interface RelationStructure<SelfT extends RelationStructure<SelfT>>
    extends PropertyHolder<SelfT> {

  default SelfT withBloomFilterFpChance(double bloomFilterFpChance) {
    return withProperty("bloom_filter_fp_chance", bloomFilterFpChance);
  }

  default SelfT withCDC(boolean enabled) {
    return withProperty("cdc", enabled);
  }

  default SelfT withCaching(KeyCaching keys, RowsPerPartition rowsPerPartition) {
    return withProperty(
        "caching",
        ImmutableMap.of(
            "keys", keys.toString(), "rows_per_partition", rowsPerPartition.getValue()));
  }

  default SelfT withComment(String comment) {
    return withProperty("comment", comment);
  }

  default SelfT withCompaction(CompactionStrategy<?> compactionStrategy) {
    return withProperty("compaction", compactionStrategy.getProperties());
  }

  default SelfT withLZ4Compression(int chunkLengthKB, double crcCheckChance) {
    return withCompression("LZ4Compressor", chunkLengthKB, crcCheckChance);
  }

  default SelfT withLZ4Compression() {
    return withCompression("LZ4Compressor");
  }

  default SelfT withSnappyCompression(int chunkLengthKB, double crcCheckChance) {
    return withCompression("SnappyCompressor", chunkLengthKB, crcCheckChance);
  }

  default SelfT withSnappyCompression() {
    return withCompression("SnappyCompressor");
  }

  default SelfT withDeflateCompression(int chunkLengthKB, double crcCheckChance) {
    return withCompression("DeflateCompressor", chunkLengthKB, crcCheckChance);
  }

  default SelfT withDeflateCompression() {
    return withCompression("DeflateCompressor");
  }

  default SelfT withCompression(String compressionAlgorithmName) {
    return withProperty("compression", ImmutableMap.of("class", compressionAlgorithmName));
  }

  default SelfT withCompression(
      String compressionAlgorithmName, int chunkLengthKB, double crcCheckChance) {
    return withProperty(
        "compression",
        ImmutableMap.of(
            "class",
            compressionAlgorithmName,
            "chunk_length_kb",
            chunkLengthKB,
            "crc_check_chance",
            crcCheckChance));
  }

  default SelfT withNoCompression() {
    return withProperty("compression", ImmutableMap.of("sstable_compression", ""));
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
}
