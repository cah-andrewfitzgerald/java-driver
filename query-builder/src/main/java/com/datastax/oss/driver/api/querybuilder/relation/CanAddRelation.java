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
package com.datastax.oss.driver.api.querybuilder.relation;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl;
import com.datastax.oss.driver.api.querybuilder.term.Term;
import com.datastax.oss.driver.internal.querybuilder.DefaultRaw;
import com.datastax.oss.driver.internal.querybuilder.relation.CustomIndexRelation;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultColumnComponentRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultColumnRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultTokenRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultTupleRelationBuilder;
import com.google.common.collect.Iterables;
import java.util.Arrays;

/** A statement that is ready to accept relations in its WHERE clause. */
public interface CanAddRelation<SelfT extends CanAddRelation<SelfT>> {

  /**
   * Adds a relation in the WHERE clause. All relations are logically joined with AND.
   *
   * <p>To create the argument, use one of the {@code isXxx} factory methods in {@link
   * QueryBuilderDsl}, for example {@link QueryBuilderDsl#isColumn(CqlIdentifier) isColumn}.
   *
   * <p>If you add multiple selectors as once, consider {@link #where(Iterable)} as a more efficient
   * alternative.
   */
  SelfT where(Relation relation);

  /**
   * Adds multiple relations at once. All relations are logically joined with AND.
   *
   * <p>This is slightly more efficient than adding the relations one by one (since the underlying
   * implementation of this object is immutable).
   *
   * <p>To create the arguments, use one of the {@code isXxx} factory methods in {@link
   * QueryBuilderDsl}, for example {@link QueryBuilderDsl#isColumn(CqlIdentifier) isColumn}.
   *
   * @see #where(Relation)
   */
  SelfT where(Iterable<Relation> additionalRelations);

  /** Var-arg equivalent of {@link #where(Iterable)}. */
  default SelfT where(Relation... additionalRelations) {
    return where(Arrays.asList(additionalRelations));
  }

  /**
   * Adds a relation testing a column.
   *
   * <p>This must be chained with an operator call, for example:
   *
   * <pre>{@code
   * selectFrom("foo").getAll().whereColumn("k").eq(bindMarker());
   * }</pre>
   *
   * This is the equivalent of creating a relation with {@link
   * QueryBuilderDsl#isColumn(CqlIdentifier)} and passing it to {@link #where(Relation)}.
   */
  default ColumnRelationBuilder<SelfT> whereColumn(CqlIdentifier id) {
    return new DefaultColumnRelationBuilder.Fluent<>(this, id);
  }

  /**
   * Shortcut for {@link #whereColumn(CqlIdentifier) whereColumn(CqlIdentifier.fromCql(name))}.
   *
   * <p>This is the equivalent of creating a relation with {@link QueryBuilderDsl#isColumn(String)}
   * and passing it to {@link #where(Relation)}.
   */
  default ColumnRelationBuilder<SelfT> whereColumn(String name) {
    return whereColumn(CqlIdentifier.fromCql(name));
  }

  /**
   * Adds a relation testing a value in a map (Cassandra 4 and above).
   *
   * <p>This is the equivalent of creating a relation with {@link
   * QueryBuilderDsl#isMapValue(CqlIdentifier, Term)} and passing it to {@link #where(Relation)}.
   */
  default ColumnComponentRelationBuilder<SelfT> whereMapValue(CqlIdentifier columnId, Term index) {
    return new DefaultColumnComponentRelationBuilder.Fluent<>(this, columnId, index);
  }

  /**
   * Shortcut for {@link #whereMapValue(CqlIdentifier, Term)
   * whereMapValue(CqlIdentifier.fromCql(columnName), index)}.
   *
   * <p>This is the equivalent of creating a relation with {@link QueryBuilderDsl#isMapValue(String,
   * Term)} and passing it to {@link #where(Relation)}.
   */
  default ColumnComponentRelationBuilder<SelfT> whereMapValue(String columnName, Term index) {
    return whereMapValue(CqlIdentifier.fromCql(columnName), index);
  }

  /**
   * Adds a relation testing a token generated from a set of columns.
   *
   * <p>This is the equivalent of creating a relation with {@link
   * QueryBuilderDsl#isTokenFromIds(Iterable)} and passing it to {@link #where(Relation)}.
   */
  default TokenRelationBuilder<SelfT> whereTokenFromIds(Iterable<CqlIdentifier> identifiers) {
    return new DefaultTokenRelationBuilder.Fluent<>(this, identifiers);
  }

  /**
   * Var-arg equivalent of {@link #whereTokenFromIds(Iterable)}.
   *
   * <p>This is the equivalent of creating a relation with {@link
   * QueryBuilderDsl#isToken(CqlIdentifier...)} and passing it to {@link #where(Relation)}.
   */
  default TokenRelationBuilder<SelfT> whereToken(CqlIdentifier... identifiers) {
    return whereTokenFromIds(Arrays.asList(identifiers));
  }

  /**
   * Equivalent of {@link #whereTokenFromIds(Iterable)} with raw strings; the names are converted
   * with {@link CqlIdentifier#fromCql(String)}.
   *
   * <p>This is the equivalent of creating a relation with {@link QueryBuilderDsl#isToken(Iterable)}
   * and passing it to {@link #where(Relation)}.
   */
  default TokenRelationBuilder<SelfT> whereToken(Iterable<String> names) {
    return whereTokenFromIds(Iterables.transform(names, CqlIdentifier::fromCql));
  }

  /**
   * Var-arg equivalent of {@link #whereToken(Iterable)}.
   *
   * <p>This is the equivalent of creating a relation with {@link
   * QueryBuilderDsl#isToken(String...)} and passing it to {@link #where(Relation)}.
   */
  default TokenRelationBuilder<SelfT> whereToken(String... names) {
    return whereToken(Arrays.asList(names));
  }

  /**
   * Adds a relation testing a set of columns, as in {@code WHERE (c1, c2, c3) IN ...}.
   *
   * <p>This is the equivalent of creating a relation with {@link
   * QueryBuilderDsl#isTupleOfIds(Iterable)} and passing it to {@link #where(Relation)}.
   */
  default TupleRelationBuilder<SelfT> whereTupleOfIds(Iterable<CqlIdentifier> identifiers) {
    return new DefaultTupleRelationBuilder.Fluent<>(this, identifiers);
  }

  /**
   * Var-arg equivalent of {@link #whereTupleOfIds(Iterable)}.
   *
   * <p>This is the equivalent of creating a relation with {@link
   * QueryBuilderDsl#isTuple(CqlIdentifier...)} and passing it to {@link #where(Relation)}.
   */
  default TupleRelationBuilder<SelfT> whereTuple(CqlIdentifier... identifiers) {
    return whereTupleOfIds(Arrays.asList(identifiers));
  }

  /**
   * Equivalent of {@link #whereTupleOfIds(Iterable)} with raw strings; the names are converted with
   * {@link CqlIdentifier#fromCql(String)}.
   *
   * <p>This is the equivalent of creating a relation with {@link QueryBuilderDsl#isTuple(Iterable)}
   * and passing it to {@link #where(Relation)}.
   */
  default TupleRelationBuilder<SelfT> whereTuple(Iterable<String> names) {
    return whereTupleOfIds(Iterables.transform(names, CqlIdentifier::fromCql));
  }

  /**
   * Var-arg equivalent of {@link #whereTuple(Iterable)}.
   *
   * <p>This is the equivalent of creating a relation with {@link
   * QueryBuilderDsl#isTuple(String...)} and passing it to {@link #where(Relation)}.
   */
  default TupleRelationBuilder<SelfT> whereTuple(String... names) {
    return whereTuple(Arrays.asList(names));
  }

  /**
   * Adds a relation on a custom index.
   *
   * <p>This is the equivalent of creating a relation with {@link
   * QueryBuilderDsl#isCustomIndex(CqlIdentifier, Term)} and passing it to {@link #where(Relation)}.
   */
  default SelfT whereCustomIndex(CqlIdentifier indexId, Term expression) {
    return where(new CustomIndexRelation(indexId, expression));
  }

  /**
   * Shortcut for {@link #whereCustomIndex(CqlIdentifier, Term)
   * whereCustomIndex(CqlIdentifier.fromCql(indexName), expression)}.
   *
   * <p>This is the equivalent of creating a relation with {@link
   * QueryBuilderDsl#isCustomIndex(String, Term)} and passing it to {@link #where(Relation)}.
   */
  default SelfT whereCustomIndex(String indexName, Term expression) {
    return whereCustomIndex(CqlIdentifier.fromCql(indexName), expression);
  }

  /**
   * Adds a raw CQL snippet as a relation.
   *
   * <p>This is the equivalent of creating a relation with {@link QueryBuilderDsl#raw(String)} and
   * passing it to {@link #where(Relation)}.
   *
   * <p>The contents will be appended to the query as-is, without any syntax checking or escaping.
   * This method should be used with caution, as it's possible to generate invalid CQL that will
   * fail at execution time; on the other hand, it can be used as a workaround to handle new CQL
   * features that are not yet covered by the query builder.
   */
  default SelfT whereRaw(String raw) {
    return where(new DefaultRaw(raw));
  }
}
