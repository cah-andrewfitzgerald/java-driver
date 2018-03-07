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
package com.datastax.oss.driver.internal.querybuilder.select;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.querybuilder.BindMarker;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.select.SelectFrom;
import com.datastax.oss.driver.api.querybuilder.select.Selector;
import com.datastax.oss.driver.internal.core.metadata.schema.ScriptBuilder;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;

public class DefaultSelect implements SelectFrom, Select {

  private static final ImmutableList<Selector> SELECT_ALL = ImmutableList.of(AllSelector.INSTANCE);

  private final CqlIdentifier keyspace;
  private final CqlIdentifier table;
  private final boolean isJson;
  private final boolean isDistinct;
  private final ImmutableList<Selector> selectors;
  private final ImmutableList<Relation> relations;
  private final ImmutableList<Selector> groupByClauses;
  private final ImmutableMap<CqlIdentifier, ClusteringOrder> orderings;
  private final Object limit;
  private final Object perPartitionLimit;
  private final boolean allowsFiltering;

  public DefaultSelect(CqlIdentifier keyspace, CqlIdentifier table) {
    this(
        keyspace,
        table,
        false,
        false,
        ImmutableList.of(),
        ImmutableList.of(),
        ImmutableList.of(),
        ImmutableMap.of(),
        null,
        null,
        false);
  }

  /**
   * This constructor is public only as a convenience for custom extensions of the query builder.
   *
   * @param selectors if it contains {@link AllSelector#INSTANCE}, that must be the only element.
   *     This isn't re-checked because methods that call this constructor internally already do it,
   *     make sure you do it yourself.
   * @param groupByClauses
   * @param orderings
   */
  public DefaultSelect(
      CqlIdentifier keyspace,
      CqlIdentifier table,
      boolean isJson,
      boolean isDistinct,
      ImmutableList<Selector> selectors,
      ImmutableList<Relation> relations,
      ImmutableList<Selector> groupByClauses,
      ImmutableMap<CqlIdentifier, ClusteringOrder> orderings,
      Object limit,
      Object perPartitionLimit,
      boolean allowsFiltering) {
    this.groupByClauses = groupByClauses;
    this.orderings = orderings;
    Preconditions.checkArgument(
        limit == null
            || (limit instanceof Integer && (Integer) limit > 0)
            || limit instanceof BindMarker,
        "limit must be a strictly positive integer or a bind marker");
    this.keyspace = keyspace;
    this.table = table;
    this.isJson = isJson;
    this.isDistinct = isDistinct;
    this.selectors = selectors;
    this.relations = relations;
    this.limit = limit;
    this.perPartitionLimit = perPartitionLimit;
    this.allowsFiltering = allowsFiltering;
  }

  @Override
  public SelectFrom json() {
    return new DefaultSelect(
        keyspace,
        table,
        true,
        isDistinct,
        selectors,
        relations,
        groupByClauses,
        orderings,
        limit,
        perPartitionLimit,
        allowsFiltering);
  }

  @Override
  public SelectFrom distinct() {
    return new DefaultSelect(
        keyspace,
        table,
        isJson,
        true,
        selectors,
        relations,
        groupByClauses,
        orderings,
        limit,
        perPartitionLimit,
        allowsFiltering);
  }

  @Override
  public Select selector(Selector selector) {
    ImmutableList<Selector> newSelectors;
    if (selector == AllSelector.INSTANCE) {
      // '*' cancels any previous one
      newSelectors = SELECT_ALL;
    } else if (SELECT_ALL.equals(selectors)) {
      // previous '*' gets cancelled
      newSelectors = ImmutableList.of(selector);
    } else {
      newSelectors = append(selectors, selector);
    }
    return withSelectors(newSelectors);
  }

  @Override
  public Select selectors(Iterable<Selector> additionalSelectors) {
    ImmutableList.Builder<Selector> newSelectors = ImmutableList.builder();
    if (!SELECT_ALL.equals(selectors)) { // previous '*' gets cancelled
      newSelectors.addAll(selectors);
    }
    for (Selector selector : additionalSelectors) {
      if (selector == AllSelector.INSTANCE) {
        throw new IllegalArgumentException("Can't pass the * selector to selectors()");
      }
      newSelectors.add(selector);
    }
    return withSelectors(newSelectors.build());
  }

  @Override
  public Select as(CqlIdentifier alias) {
    if (SELECT_ALL.equals(selectors)) {
      throw new IllegalStateException("Can't alias the * selector");
    } else if (selectors.isEmpty()) {
      throw new IllegalStateException("Can't alias, no selectors defined");
    }
    return withSelectors(modifyLast(selectors, last -> last.as(alias)));
  }

  public Select withSelectors(ImmutableList<Selector> newSelectors) {
    return new DefaultSelect(
        keyspace,
        table,
        isJson,
        isDistinct,
        newSelectors,
        relations,
        groupByClauses,
        orderings,
        limit,
        perPartitionLimit,
        allowsFiltering);
  }

  @Override
  public Select where(Relation relation) {
    return withRelations(append(relations, relation));
  }

  @Override
  public Select where(Iterable<Relation> additionalRelations) {
    return withRelations(concat(relations, additionalRelations));
  }

  public Select withRelations(ImmutableList<Relation> newRelations) {
    return new DefaultSelect(
        keyspace,
        table,
        isJson,
        isDistinct,
        selectors,
        newRelations,
        groupByClauses,
        orderings,
        limit,
        perPartitionLimit,
        allowsFiltering);
  }

  @Override
  public Select groupBy(Selector groupByClause) {
    return withGroupByClauses(append(groupByClauses, groupByClause));
  }

  @Override
  public Select groupBy(Iterable<Selector> newGroupByClauses) {
    return withGroupByClauses(concat(groupByClauses, newGroupByClauses));
  }

  public Select withGroupByClauses(ImmutableList<Selector> newGroupByClauses) {
    return new DefaultSelect(
        keyspace,
        table,
        isJson,
        isDistinct,
        selectors,
        relations,
        newGroupByClauses,
        orderings,
        limit,
        perPartitionLimit,
        allowsFiltering);
  }

  @Override
  public Select orderBy(CqlIdentifier columnId, ClusteringOrder order) {
    return withOrderings(append(orderings, columnId, order));
  }

  @Override
  public Select orderByIds(Map<CqlIdentifier, ClusteringOrder> newOrderings) {
    return withOrderings(concat(orderings, newOrderings));
  }

  public Select withOrderings(ImmutableMap<CqlIdentifier, ClusteringOrder> newOrderings) {
    return new DefaultSelect(
        keyspace,
        table,
        isJson,
        isDistinct,
        selectors,
        relations,
        groupByClauses,
        newOrderings,
        limit,
        perPartitionLimit,
        allowsFiltering);
  }

  @Override
  public Select limit(int limit) {
    Preconditions.checkArgument(limit > 0, "Limit must be strictly positive");
    return new DefaultSelect(
        keyspace,
        table,
        isJson,
        isDistinct,
        selectors,
        relations,
        groupByClauses,
        orderings,
        limit,
        perPartitionLimit,
        allowsFiltering);
  }

  @Override
  public Select limit(BindMarker bindMarker) {
    Preconditions.checkNotNull(bindMarker);
    return new DefaultSelect(
        keyspace,
        table,
        isJson,
        isDistinct,
        selectors,
        relations,
        groupByClauses,
        orderings,
        bindMarker,
        perPartitionLimit,
        allowsFiltering);
  }

  @Override
  public Select perPartitionLimit(int perPartitionLimit) {
    Preconditions.checkArgument(
        perPartitionLimit > 0, "perPartitionLimit must be strictly positive");
    return new DefaultSelect(
        keyspace,
        table,
        isJson,
        isDistinct,
        selectors,
        relations,
        groupByClauses,
        orderings,
        limit,
        perPartitionLimit,
        allowsFiltering);
  }

  @Override
  public Select perPartitionLimit(BindMarker bindMarker) {
    Preconditions.checkNotNull(bindMarker);
    return new DefaultSelect(
        keyspace,
        table,
        isJson,
        isDistinct,
        selectors,
        relations,
        groupByClauses,
        orderings,
        limit,
        bindMarker,
        allowsFiltering);
  }

  @Override
  public Select allowFiltering() {
    return new DefaultSelect(
        keyspace,
        table,
        isJson,
        isDistinct,
        selectors,
        relations,
        groupByClauses,
        orderings,
        limit,
        perPartitionLimit,
        true);
  }

  @Override
  public SimpleStatement build(boolean pretty) {
    // TODO move ScriptBuilder to a more generic package (util?), it's now used in multiple places
    ScriptBuilder builder = new ScriptBuilder(pretty);

    builder.append("SELECT");
    if (isJson) {
      builder.append(" JSON");
    }
    if (isDistinct) {
      builder.append(" DISTINCT");
    }

    builder.increaseIndent();
    for (int i = 0; i < selectors.size(); i++) {
      if (i > 0) {
        builder.append(",");
      }
      builder.newLine().append(selectors.get(i).asCql(pretty));
    }
    builder.decreaseIndent();

    builder.newLine().append("FROM ");
    if (keyspace != null) {
      builder.append(keyspace).append(".");
    }
    builder.append(table);

    if (!relations.isEmpty()) {
      builder.newLine().append("WHERE ").append(relations.get(0).asCql(pretty));
      for (int i = 1; i < relations.size(); i++) {
        builder.newLine().append("AND ").append(relations.get(i).asCql(pretty));
      }
    }

    if (!groupByClauses.isEmpty()) {
      builder.newLine().append("GROUP BY ").append(groupByClauses.get(0).asCql(pretty));
      for (int i = 1; i < groupByClauses.size(); i++) {
        builder.append(",").newLine().append(groupByClauses.get(i).asCql(pretty));
      }
    }

    if (!orderings.isEmpty()) {
      builder.newLine().append("ORDER BY");
      boolean first = true;
      for (Map.Entry<CqlIdentifier, ClusteringOrder> entry : orderings.entrySet()) {
        if (first) {
          builder.append(" ");
          first = false;
        } else {
          builder.append(",").newLine();
        }
        builder.append(entry.getKey().asCql(pretty)).append(" ").append(entry.getValue().name());
      }
    }

    if (limit != null) {
      builder
          .newLine()
          .append("LIMIT ")
          .append(
              (limit instanceof BindMarker)
                  ? ((BindMarker) limit).asCql(pretty)
                  : limit.toString());
    }
    if (perPartitionLimit != null) {
      builder
          .newLine()
          .append("PER PARTITION LIMIT ")
          .append(
              (perPartitionLimit instanceof BindMarker)
                  ? ((BindMarker) perPartitionLimit).asCql(pretty)
                  : perPartitionLimit.toString());
    }
    if (allowsFiltering) {
      builder.newLine().append("ALLOW FILTERING");
    }

    // TODO compute idempotence
    return SimpleStatement.newInstance(builder.build());
  }

  public CqlIdentifier getKeyspace() {
    return keyspace;
  }

  public CqlIdentifier getTable() {
    return table;
  }

  public boolean isJson() {
    return isJson;
  }

  public boolean isDistinct() {
    return isDistinct;
  }

  public ImmutableList<Selector> getSelectors() {
    return selectors;
  }

  public ImmutableList<Relation> getRelations() {
    return relations;
  }

  public ImmutableList<Selector> getGroupByClauses() {
    return groupByClauses;
  }

  public ImmutableMap<CqlIdentifier, ClusteringOrder> getOrderings() {
    return orderings;
  }

  public Object getLimit() {
    return limit;
  }

  public Object getPerPartitionLimit() {
    return perPartitionLimit;
  }

  public boolean allowsFiltering() {
    return allowsFiltering;
  }

  // TODO will likely be used by other queries, move elsewhere
  private static <T> ImmutableList<T> append(ImmutableList<T> list, T newElement) {
    return ImmutableList.<T>builder().addAll(list).add(newElement).build();
  }

  private static <T> ImmutableList<T> concat(ImmutableList<T> list1, Iterable<T> list2) {
    return ImmutableList.<T>builder().addAll(list1).addAll(list2).build();
  }

  private static <K, V> ImmutableMap<K, V> append(ImmutableMap<K, V> map, K newKey, V newValue) {
    ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
    for (Map.Entry<K, V> entry : map.entrySet()) {
      if (!entry.getKey().equals(newKey)) {
        builder.put(entry);
      }
    }
    builder.put(newKey, newValue);
    return builder.build();
  }

  private static <K, V> ImmutableMap<K, V> concat(ImmutableMap<K, V> map1, Map<K, V> map2) {
    ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
    for (Map.Entry<K, V> entry : map1.entrySet()) {
      if (!map2.containsKey(entry.getKey())) {
        builder.put(entry);
      }
    }
    builder.putAll(map2);
    return builder.build();
  }

  private static <T> ImmutableList<T> modifyLast(ImmutableList<T> list, Function<T, T> change) {
    ImmutableList.Builder<T> builder = ImmutableList.builder();
    int size = list.size();
    for (int i = 0; i < size - 1; i++) {
      builder.add(list.get(i));
    }
    builder.add(change.apply(list.get(size - 1)));
    return builder.build();
  }
}
