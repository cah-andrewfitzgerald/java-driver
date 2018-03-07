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
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl;
import com.datastax.oss.driver.api.querybuilder.term.Term;
import java.util.Arrays;
import java.util.Map;

/**
 * A SELECT query that accepts additional selectors (that is, elements in the SELECT clause to
 * return as columns in the result set, as in: {@code SELECT count(*), sku, price...}).
 */
public interface CanAddSelector {

  /**
   * Adds a selector.
   *
   * <p>To create the argument, use one of the {@code getXxx} factory methods in {@link
   * QueryBuilderDsl}, for example {@link QueryBuilderDsl#getColumn(CqlIdentifier) getColumn}. This
   * type also provides shortcuts to create and add the selector in one call, for example {@link
   * #column(CqlIdentifier)} for {@code selector(getColumn(...))}.
   *
   * <p>If you add multiple selectors as once, consider {@link #selectors(Iterable)} as a more
   * efficient alternative.
   */
  Select selector(Selector selector);

  /**
   * Adds multiple selectors at once.
   *
   * <p>This is slightly more efficient than adding the selectors one by one (since the underlying
   * implementation of this object is immutable).
   *
   * <p>To create the arguments, use one of the {@code getXxx} factory methods in {@link
   * QueryBuilderDsl}, for example {@link QueryBuilderDsl#getColumn(CqlIdentifier) getColumn}.
   *
   * @throws IllegalArgumentException if one of the selectors is {@link QueryBuilderDsl#getAll()}
   *     ({@code *} can only be used on its own).
   * @see #selector(Selector)
   */
  Select selectors(Iterable<Selector> additionalSelectors);

  /** Var-arg equivalent of {@link #selectors(Iterable)}. */
  default Select selectors(Selector... additionalSelectors) {
    return selectors(Arrays.asList(additionalSelectors));
  }

  /**
   * Selects all columns, as in {@code SELECT *}.
   *
   * <p>This will clear any previously configured selector. Similarly, if any other selector is
   * added later, it will cancel this one.
   *
   * <p>This is a shortcut for {@link #selector(Selector) selector(QueryBuilderDsl.getAll())}.
   *
   * @see QueryBuilderDsl#getAll()
   */
  default Select all() {
    return selector(QueryBuilderDsl.getAll());
  }

  /**
   * Selects the count of all returned rows, as in {@code SELECT count(*)}.
   *
   * <p>This is a shortcut for {@link #selector(Selector) selector(QueryBuilderDsl.getCountAll())}.
   *
   * @see QueryBuilderDsl#getCountAll()
   */
  default Select countAll() {
    return selector(QueryBuilderDsl.getCountAll());
  }

  /**
   * Selects a particular column by its CQL identifier.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getColumn(columnId))}.
   *
   * @see QueryBuilderDsl#getColumn(CqlIdentifier)
   */
  default Select column(CqlIdentifier columnId) {
    return selector(QueryBuilderDsl.getColumn(columnId));
  }

  /** Shortcut for {@link #column(CqlIdentifier) column(CqlIdentifier.fromCql(columnName))} */
  default Select column(String columnName) {
    return column(CqlIdentifier.fromCql(columnName));
  }

  /**
   * Selects the sum of two arguments, as in {@code SELECT col1 + col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>This is a shortcut for {@link #selector(Selector) selector(QueryBuilderDsl.getSum(left,
   * right))}.
   *
   * @see QueryBuilderDsl#getSum(Selector, Selector)
   */
  default Select sum(Selector left, Selector right) {
    return selector(QueryBuilderDsl.getSum(left, right));
  }

  /**
   * Selects the difference of two terms, as in {@code SELECT col1 - col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getDifference(left, right))}.
   *
   * @see QueryBuilderDsl#getDifference(Selector, Selector)
   */
  default Select difference(Selector left, Selector right) {
    return selector(QueryBuilderDsl.getDifference(left, right));
  }

  /**
   * Selects the product of two arguments, as in {@code SELECT col1 * col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>This is a shortcut for {@link #selector(Selector) selector(QueryBuilderDsl.getProduct(left,
   * right))}.
   *
   * <p>The arguments will be parenthesized if they are instances of {@link QueryBuilderDsl#getSum}
   * or {@link QueryBuilderDsl#getDifference}. If they are raw selectors, you might have to
   * parenthesize them yourself.
   *
   * @see QueryBuilderDsl#getProduct(Selector, Selector)
   */
  default Select product(Selector left, Selector right) {
    return selector(QueryBuilderDsl.getProduct(left, right));
  }

  /**
   * Selects the quotient of two arguments, as in {@code SELECT col1 / col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>This is a shortcut for {@link #selector(Selector) selector(QueryBuilderDsl.getQuotient(left,
   * right))}.
   *
   * <p>The arguments will be parenthesized if they are instances of {@link QueryBuilderDsl#getSum}
   * or {@link QueryBuilderDsl#getDifference}. If they are raw selectors, you might have to
   * parenthesize them yourself.
   *
   * @see QueryBuilderDsl#getQuotient(Selector, Selector)
   */
  default Select quotient(Selector left, Selector right) {
    return selector(QueryBuilderDsl.getQuotient(left, right));
  }

  /**
   * Selects the remainder of two arguments, as in {@code SELECT col1 % col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getRemainder(left, right))}.
   *
   * <p>The arguments will be parenthesized if they are instances of {@link QueryBuilderDsl#getSum}
   * or {@link QueryBuilderDsl#getDifference}. If they are raw selectors, you might have to
   * parenthesize them yourself.
   *
   * @see QueryBuilderDsl#getRemainder(Selector, Selector)
   */
  default Select remainder(Selector left, Selector right) {
    return selector(QueryBuilderDsl.getRemainder(left, right));
  }

  /**
   * Selects the opposite of an argument, as in {@code SELECT -col1}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getOpposite(argument))}.
   *
   * <p>The argument will be parenthesized if it is an instance of {@link QueryBuilderDsl#getSum} or
   * {@link QueryBuilderDsl#getDifference}. If it is a raw selector, you might have to parenthesize
   * it yourself.
   *
   * @see QueryBuilderDsl#getOpposite(Selector)
   */
  default Select opposite(Selector argument) {
    return selector(QueryBuilderDsl.getOpposite(argument));
  }

  /**
   * Selects a field inside of a UDT column, as in {@code SELECT user.name}.
   *
   * <p>This is a shortcut for {@link #selector(Selector) selector(QueryBuilderDsl.getField(udt,
   * fieldId))}.
   *
   * @see QueryBuilderDsl#getField(Selector, CqlIdentifier)
   */
  default Select field(Selector udt, CqlIdentifier fieldId) {
    return selector(QueryBuilderDsl.getField(udt, fieldId));
  }

  /**
   * Shortcut for {@link #field(Selector, CqlIdentifier) field(udt,
   * CqlIdentifier.fromCql(fieldName))}.
   */
  default Select field(Selector udt, String fieldName) {
    return field(udt, CqlIdentifier.fromCql(fieldName));
  }

  /**
   * Shortcut to select a UDT field when the UDT is a simple column (as opposed to a more complex
   * selection, like a nested UDT).
   *
   * <p>In other words, this is a shortcut for {{@link #field(Selector, CqlIdentifier)
   * field(QueryBuilderDsl.getColumn(udtColumnId), fieldId)}.
   *
   * @see QueryBuilderDsl#getField(CqlIdentifier, CqlIdentifier)
   */
  default Select field(CqlIdentifier udtColumnId, CqlIdentifier fieldId) {
    return field(QueryBuilderDsl.getColumn(udtColumnId), fieldId);
  }

  /**
   * Shortcut for {@link #field(CqlIdentifier, CqlIdentifier)
   * field(CqlIdentifier.fromCql(udtColumnName), CqlIdentifier.fromCql(fieldName))}.
   *
   * @see QueryBuilderDsl#getField(String, String)
   */
  default Select field(String udtColumnName, String fieldName) {
    return field(CqlIdentifier.fromCql(udtColumnName), CqlIdentifier.fromCql(fieldName));
  }

  /**
   * Selects an element in a collection column, as in {@code SELECT m['key']}.
   *
   * <p>As of Cassandra 4, this is only allowed for map and set columns.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getElement(collection, index))}.
   *
   * @see QueryBuilderDsl#getElement(Selector, Term)
   */
  default Select element(Selector collection, Term index) {
    return selector(QueryBuilderDsl.getElement(collection, index));
  }

  /**
   * Shortcut for element selection when the target collection is a simple column.
   *
   * <p>In other words, this is the equivalent of {@link #element(Selector, Term)
   * element(QueryBuilderDsl.getColumn(collection), index)}.
   *
   * @see QueryBuilderDsl#getElement(CqlIdentifier, Term)
   */
  default Select element(CqlIdentifier collectionId, Term index) {
    return element(QueryBuilderDsl.getColumn(collectionId), index);
  }

  /**
   * Shortcut for {@link #element(CqlIdentifier, Term)
   * element(CqlIdentifier.fromCql(collectionName), index)}.
   *
   * @see QueryBuilderDsl#getElement(String, Term)
   */
  default Select element(String collectionName, Term index) {
    return element(CqlIdentifier.fromCql(collectionName), index);
  }

  /**
   * Selects a slice in a collection column, as in {@code SELECT s[4..8]}.
   *
   * <p>As of Cassandra 4, this is only allowed for set and map columns. Those collections are
   * ordered, the elements (or keys in the case of a map), will be compared to the bounds for
   * inclusions. Either bound can be unspecified, but not both.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getRange(collection, left, right))}.
   *
   * @param left the left bound (inclusive). Can be {@code null} to indicate that the slice is only
   *     right-bound.
   * @param right the right bound (inclusive). Can be {@code null} to indicate that the slice is
   *     only left-bound.
   * @see QueryBuilderDsl#getRange(Selector, Term, Term)
   */
  default Select range(Selector collection, Term left, Term right) {
    return selector(QueryBuilderDsl.getRange(collection, left, right));
  }

  /**
   * Shortcut for slice selection when the target collection is a simple column.
   *
   * <p>In other words, this is the equivalent of {@link #range(Selector, Term, Term)}
   * range(QueryBuilderDsl.getColumn(collectionId), left, right)}.
   *
   * @see QueryBuilderDsl#getRange(CqlIdentifier, Term, Term)
   */
  default Select range(CqlIdentifier collectionId, Term left, Term right) {
    return range(QueryBuilderDsl.getColumn(collectionId), left, right);
  }

  /**
   * Shortcut for {@link #range(CqlIdentifier, Term, Term)
   * range(CqlIdentifier.fromCql(collectionName), left, right)}.
   *
   * @see QueryBuilderDsl#getRange(String, Term, Term)
   */
  default Select range(String collectionName, Term left, Term right) {
    return range(CqlIdentifier.fromCql(collectionName), left, right);
  }

  /**
   * Selects a group of elements as a list, as in {@code SELECT [a,b,c]}.
   *
   * <p>None of the selectors should be aliased (the query builder checks this at runtime), and they
   * should all produce the same data type (the query builder can't check this, so the query will
   * fail at execution time).
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getListOf(elementSelectors))}.
   *
   * @throws IllegalArgumentException if any of the selectors is aliased.
   * @see QueryBuilderDsl#getListOf(Iterable)
   */
  default Select listOf(Iterable<Selector> elementSelectors) {
    return selector(QueryBuilderDsl.getListOf(elementSelectors));
  }

  /** Var-arg equivalent of {@link #listOf(Iterable)}. */
  default Select listOf(Selector... elementSelectors) {
    return listOf(Arrays.asList(elementSelectors));
  }

  /**
   * Selects a group of elements as a set, as in {@code SELECT {a,b,c}}.
   *
   * <p>None of the selectors should be aliased (the query builder checks this at runtime), and they
   * should all produce the same data type (the query builder can't check this, so the query will
   * fail at execution time).
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getSetOf(elementSelectors))}.
   *
   * @throws IllegalArgumentException if any of the selectors is aliased.
   * @see QueryBuilderDsl#getSetOf(Iterable)
   */
  default Select setOf(Iterable<Selector> elementSelectors) {
    return selector(QueryBuilderDsl.getSetOf(elementSelectors));
  }

  /** Var-arg equivalent of {@link #setOf(Iterable)}. */
  default Select setOf(Selector... elementSelectors) {
    return setOf(Arrays.asList(elementSelectors));
  }

  /**
   * Selects a group of elements as a tuple, as in {@code SELECT (a,b,c)}.
   *
   * <p>None of the selectors should be aliased (the query builder checks this at runtime).
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getTupleOf(elementSelectors))}.
   *
   * @throws IllegalArgumentException if any of the selectors is aliased.
   * @see QueryBuilderDsl#getTupleOf(Iterable)
   */
  default Select tupleOf(Iterable<Selector> elementSelectors) {
    return selector(QueryBuilderDsl.getTupleOf(elementSelectors));
  }

  /** Var-arg equivalent of {@link #tupleOf(Iterable)}. */
  default Select tupleOf(Selector... elementSelectors) {
    return tupleOf(Arrays.asList(elementSelectors));
  }

  /**
   * Selects a group of elements as a map, as in {@code SELECT {a:b,c:d}}.
   *
   * <p>None of the selectors should be aliased (the query builder checks this at runtime). In
   * addition, all key selectors should produce the same type, and all value selectors as well (the
   * key and value types can be different); the query builder can't check this, so the query will
   * fail at execution time if the types are not uniform.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getMapOf(elementSelectors))}.
   *
   * <p>Note that Cassandra often has trouble inferring the exact map type. This will manifest as
   * the error message:
   *
   * <pre>
   *   Cannot infer type for term xxx in selection clause (try using a cast to force a type)
   * </pre>
   *
   * If you run into this, consider providing the types explicitly with {@link #mapOf(Map, DataType,
   * DataType)}.
   *
   * @throws IllegalArgumentException if any of the selectors is aliased.
   * @see QueryBuilderDsl#getMapOf(Map)
   */
  default Select mapOf(Map<Selector, Selector> elementSelectors) {
    return selector(QueryBuilderDsl.getMapOf(elementSelectors));
  }

  /**
   * Selects a group of elements as a map and force the resulting map type, as in {@code SELECT
   * (map<int,text>){a:b,c:d}}.
   *
   * <p>Use the constants and static methods in {@link DataTypes} to create the data types.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getMapOf(elementSelectors, keyType, valueType))}.
   *
   * @see #mapOf(Map)
   * @see QueryBuilderDsl#getMapOf(Map, DataType, DataType)
   */
  default Select mapOf(
      Map<Selector, Selector> elementSelectors, DataType keyType, DataType valueType) {
    return selector(QueryBuilderDsl.getMapOf(elementSelectors, keyType, valueType));
  }

  /**
   * Forces a selector to a particular type, as in {@code SELECT (int)a}.
   *
   * <p>Use the constants and static methods in {@link DataTypes} to create the data type.
   *
   * <p>This is a shortcut for {@link #selector(Selector) selector(QueryBuilderDsl.getCast(selector,
   * targetType))}.
   *
   * @see QueryBuilderDsl#getCast(Selector, DataType)
   */
  default Select cast(Selector selector, DataType targetType) {
    return selector(QueryBuilderDsl.getCast(selector, targetType));
  }

  /**
   * Selects the result of a function call, as is {@code SELECT f(a,b)}
   *
   * <p>None of the arguments should be aliased (the query builder checks this at runtime).
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getFunction(functionId, arguments))}.
   *
   * @throws IllegalArgumentException if any of the selectors is aliased.
   * @see QueryBuilderDsl#getFunction(CqlIdentifier, Iterable)
   */
  default Select function(CqlIdentifier functionId, Iterable<Selector> arguments) {
    return selector(QueryBuilderDsl.getFunction(functionId, arguments));
  }

  /**
   * Var-arg equivalent of {@link #function(CqlIdentifier, Iterable)}.
   *
   * @see QueryBuilderDsl#getFunction(CqlIdentifier, Selector...)
   */
  default Select function(CqlIdentifier functionId, Selector... arguments) {
    return function(functionId, Arrays.asList(arguments));
  }

  /**
   * Shortcut for {@link #function(CqlIdentifier, Iterable)
   * function(CqlIdentifier.fromCql(functionName), arguments)}.
   *
   * @see QueryBuilderDsl#getFunction(String, Iterable)
   */
  default Select function(String functionName, Iterable<Selector> arguments) {
    return function(CqlIdentifier.fromCql(functionName), arguments);
  }

  /**
   * Var-arg equivalent of {@link #function(String, Iterable)}.
   *
   * @see QueryBuilderDsl#getFunction(String, Selector...)
   */
  default Select function(String functionName, Selector... arguments) {
    return function(functionName, Arrays.asList(arguments));
  }

  /**
   * Selects the result of a function call, as is {@code SELECT f(a,b)}
   *
   * <p>None of the arguments should be aliased (the query builder checks this at runtime).
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getFunction(keyspaceId, functionId, arguments))}.
   *
   * @throws IllegalArgumentException if any of the selectors is aliased.
   * @see QueryBuilderDsl#getFunction(CqlIdentifier,CqlIdentifier, Iterable)
   */
  default Select function(
      CqlIdentifier keyspaceId, CqlIdentifier functionId, Iterable<Selector> arguments) {
    return selector(QueryBuilderDsl.getFunction(keyspaceId, functionId, arguments));
  }

  /**
   * Var-arg equivalent of {@link #function(CqlIdentifier,CqlIdentifier, Iterable)}.
   *
   * @see QueryBuilderDsl#getFunction(CqlIdentifier,CqlIdentifier, Selector...)
   */
  default Select function(
      CqlIdentifier keyspaceId, CqlIdentifier functionId, Selector... arguments) {
    return function(keyspaceId, functionId, Arrays.asList(arguments));
  }

  /**
   * Shortcut for {@link #function(CqlIdentifier, CqlIdentifier, Iterable)
   * function(CqlIdentifier.fromCql(keyspaceName), CqlIdentifier.fromCql(functionName), arguments)}.
   *
   * @see QueryBuilderDsl#getFunction(String,String, Iterable)
   */
  default Select function(String keyspaceName, String functionName, Iterable<Selector> arguments) {
    return function(
        CqlIdentifier.fromCql(keyspaceName), CqlIdentifier.fromCql(functionName), arguments);
  }

  /**
   * Var-arg equivalent of {@link #function(String, String, Iterable)}.
   *
   * @see QueryBuilderDsl#getFunction(String, String, Selector...)
   */
  default Select function(String keyspaceName, String functionName, Selector... arguments) {
    return function(keyspaceName, functionName, Arrays.asList(arguments));
  }

  /**
   * Shortcut to select the result of the built-in {@code writetime} function, as in {@code SELECT
   * writetime(c)}.
   *
   * @see QueryBuilderDsl#getWriteTime(CqlIdentifier)
   */
  default Select writeTime(CqlIdentifier columnId) {
    return selector(QueryBuilderDsl.getWriteTime(columnId));
  }

  /**
   * Shortcut for {@link #writeTime(CqlIdentifier) writeTime(CqlIdentifier.fromCql(columnName))}.
   *
   * @see QueryBuilderDsl#getWriteTime(String)
   */
  default Select writeTime(String columnName) {
    return writeTime(CqlIdentifier.fromCql(columnName));
  }

  /**
   * Shortcut to select the result of the built-in {@code ttl} function, as in {@code SELECT
   * ttl(c)}.
   *
   * @see QueryBuilderDsl#getTtl(CqlIdentifier)
   */
  default Select ttl(CqlIdentifier columnId) {
    return selector(QueryBuilderDsl.getTtl(columnId));
  }

  /**
   * Shortcut for {@link #ttl(CqlIdentifier) ttl(CqlIdentifier.fromCql(columnName))}.
   *
   * @see QueryBuilderDsl#getTtl(String)
   */
  default Select ttl(String columnName) {
    return ttl(CqlIdentifier.fromCql(columnName));
  }

  /**
   * Selects an arbitrary expression expressed as a raw string.
   *
   * <p>The contents will be appended to the query as-is, without any syntax checking or escaping.
   * This method should be used with caution, as it's possible to generate invalid CQL that will
   * fail at execution time; on the other hand, it can be used as a workaround to handle new CQL
   * features that are not yet covered by the query builder.
   *
   * <p>This is a shortcut for {@link #selector(Selector)
   * selector(QueryBuilderDsl.getRaw(rawExpression))}.
   */
  default Select raw(String rawExpression) {
    return selector(QueryBuilderDsl.raw(rawExpression));
  }

  /**
   * Aliases the last added selector, as in {@code SELECT count(*) AS total}.
   *
   * <p>It is the caller's responsibility to ensure that this method is called at most once after
   * each selector, and that this selector can legally be aliased:
   *
   * <ul>
   *   <li>if it is called multiple times ({@code countAll().as("total1").as("total2")}), the last
   *       alias will override the previous ones.
   *   <li>if it is called before any selector was set, or after {@link #all()}, an {@link
   *       IllegalStateException} is thrown.
   *   <li>if it is called after a {@link #raw(String)} selector that already defines an alias, the
   *       query will fail at runtime.
   * </ul>
   */
  Select as(CqlIdentifier alias);

  /** Shortcut for {@link #as(CqlIdentifier) as(CqlIdentifier.fromCql(alias))} */
  default Select as(String alias) {
    return as(CqlIdentifier.fromCql(alias));
  }
}
