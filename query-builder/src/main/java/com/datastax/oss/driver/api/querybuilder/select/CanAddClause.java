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
package com.datastax.oss.driver.api.querybuilder.select;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.querybuilder.BindMarker;
import com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Map;

/**
 * A SELECT query that accepts additional clauses: WHERE, GROUP BY, ORDER BY, LIMIT, PER PARTITION
 * LIMIT, ALLOW FILTERING.
 */
public interface CanAddClause {

  // Implementation note - this interface is separate from CanAddSelector to make the following a
  // compile-time error:
  // selectFrom("foo").allowFiltering().build()

  /**
   * Adds a relation in the WHERE clause. All relations are logically joined with AND.
   *
   * <p>To create the argument, use one of the {@code isXxx} factory methods in {@link
   * QueryBuilderDsl}, for example {@link QueryBuilderDsl#isColumn(CqlIdentifier) isColumn}.
   *
   * <p>If you add multiple selectors as once, consider {@link #where(Iterable)} as a more efficient
   * alternative.
   */
  Select where(Relation relation);

  /**
   * Adds multiple relations at once. All relations are logically joined with AND.
   *
   * <p>This is slightly more efficient than adding the relations one by one (since the underlying
   * implementation of this object is immutable).
   *
   * <p>To create the argument, use one of the {@code isXxx} factory methods in {@link
   * QueryBuilderDsl}, for example {@link QueryBuilderDsl#isColumn(CqlIdentifier) isColumn}.
   *
   * @see #where(Relation)
   */
  Select where(Iterable<Relation> additionalRelations);

  /** Var-arg equivalent of {@link #where(Iterable)}. */
  default Select where(Relation... additionalRelations) {
    return where(Arrays.asList(additionalRelations));
  }

  /**
   * Adds the provided GROUP BY clauses to the query.
   *
   * <p>As of version 4.0, Apache Cassandra only allows grouping by columns, therefore you can use
   * the shortcuts {@link #groupByColumns(Iterable)} or {@link #groupByColumnIds(Iterable)}.
   */
  Select groupBy(Iterable<Selector> selectors);

  /** Var-arg equivalent of {@link #groupBy(Iterable)}. */
  default Select groupBy(Selector... selectors) {
    return groupBy(Arrays.asList(selectors));
  }

  /**
   * Shortcut for {@link #groupBy(Iterable)} where all the clauses are simple columns. The arguments
   * are wrapped with {@link QueryBuilderDsl#getColumn(CqlIdentifier)}.
   */
  default Select groupByColumnIds(Iterable<CqlIdentifier> columnIds) {
    return groupBy(Iterables.transform(columnIds, QueryBuilderDsl::getColumn));
  }

  /** Var-arg equivalent of {@link #groupByColumnIds(Iterable)}. */
  default Select groupByColumnIds(CqlIdentifier... columnIds) {
    return groupByColumnIds(Arrays.asList(columnIds));
  }

  /**
   * Shortcut for {@link #groupBy(Iterable)} where all the clauses are simple columns. The arguments
   * are wrapped with {@link QueryBuilderDsl#getColumn(String)}.
   */
  default Select groupByColumns(Iterable<String> columnNames) {
    return groupBy(Iterables.transform(columnNames, QueryBuilderDsl::getColumn));
  }

  /** Var-arg equivalent of {@link #groupByColumns(Iterable)}. */
  default Select groupByColumns(String... columnNames) {
    return groupByColumns(Arrays.asList(columnNames));
  }

  /**
   * Adds the provided GROUP BY clause to the query.
   *
   * <p>As of version 4.0, Apache Cassandra only allows grouping by columns, therefore you can use
   * the shortcuts {@link #groupBy(String)} or {@link #groupBy(CqlIdentifier)}.
   */
  Select groupBy(Selector selector);

  /** Shortcut for {@link #groupBy(Selector) groupBy(QueryBuilderDsl.getColumn(columnId))}. */
  default Select groupBy(CqlIdentifier columnId) {
    return groupBy(QueryBuilderDsl.getColumn(columnId));
  }

  /** Shortcut for {@link #groupBy(Selector) groupBy(QueryBuilderDsl.getColumn(columnName))}. */
  default Select groupBy(String columnName) {
    return groupBy(QueryBuilderDsl.getColumn(columnName));
  }

  /**
   * Adds the provided ORDER BY clauses to the query.
   *
   * <p>They will be appended in the iteration order of the provided map. If an ordering was already
   * defined for a given identifier, it will be removed and the new ordering will appear in its
   * position in the provided map.
   */
  Select orderByIds(Map<CqlIdentifier, ClusteringOrder> orderings);

  /**
   * Shortcut for {@link #orderByIds(Map)} with the columns specified as case-insensitive names.
   * They will be wrapped with {@link CqlIdentifier#fromCql(String)}.
   *
   * <p>Note that it's possible for two different case-insensitive names to resolve to the same
   * identifier, for example "foo" and "Foo"; if this happens, a runtime exception will be thrown.
   *
   * @throws IllegalArgumentException if two names resolve to the same identifier.
   */
  default Select orderBy(Map<String, ClusteringOrder> orderings) {
    ImmutableMap.Builder<CqlIdentifier, ClusteringOrder> builder = ImmutableMap.builder();
    for (Map.Entry<String, ClusteringOrder> entry : orderings.entrySet()) {
      builder.put(CqlIdentifier.fromCql(entry.getKey()), entry.getValue());
    }
    // build() throws if there are duplicate keys
    return orderByIds(builder.build());
  }

  /**
   * Adds the provided ORDER BY clause to the query.
   *
   * <p>If an ordering was already defined for this identifier, it will be removed and the new
   * clause will be appended at the end of the current list for this query.
   */
  Select orderBy(CqlIdentifier columnId, ClusteringOrder order);

  /**
   * Shortcut for {@link #orderBy(CqlIdentifier, ClusteringOrder)
   * orderBy(CqlIdentifier.fromCql(columnName), order)}.
   */
  default Select orderBy(String columnName, ClusteringOrder order) {
    return orderBy(CqlIdentifier.fromCql(columnName), order);
  }

  /**
   * Adds a LIMIT clause to this query with a literal value.
   *
   * <p>If this method or {@link #limit(BindMarker)} is called multiple times, the last value is
   * used.
   */
  Select limit(int limit);

  /**
   * Adds a LIMIT clause to this query with a bind marker.
   *
   * <p>To create the argument, use one of the factory methods in {@link QueryBuilderDsl}, for
   * example {@link QueryBuilderDsl#bindMarker() bindMarker()}.
   *
   * <p>If this method or {@link #limit(int)} is called multiple times, the last value is used.
   */
  Select limit(BindMarker bindMarker);

  /**
   * Adds a PER PARTITION LIMIT clause to this query with a literal value.
   *
   * <p>If this method or {@link #perPartitionLimit(BindMarker)} is called multiple times, the last
   * value is used.
   */
  Select perPartitionLimit(int limit);

  /**
   * Adds a PER PARTITION LIMIT clause to this query with a bind marker.
   *
   * <p>To create the argument, use one of the factory methods in {@link QueryBuilderDsl}, for
   * example {@link QueryBuilderDsl#bindMarker() bindMarker()}.
   *
   * <p>If this method or {@link #perPartitionLimit(int)} is called multiple times, the last value
   * is used.
   */
  Select perPartitionLimit(BindMarker bindMarker);

  /**
   * Adds an ALLOW FILTERING clause to this query.
   *
   * <p>This method is idempotent, calling it multiple times will only add a single clause.
   */
  Select allowFiltering();
}