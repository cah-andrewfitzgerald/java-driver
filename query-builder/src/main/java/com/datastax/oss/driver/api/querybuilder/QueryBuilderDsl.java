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
package com.datastax.oss.driver.api.querybuilder;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.codec.CodecNotFoundException;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry;
import com.datastax.oss.driver.api.querybuilder.relation.ColumnComponentRelationBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.ColumnRelationBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.oss.driver.api.querybuilder.relation.TokenRelationBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.TupleRelationBuilder;
import com.datastax.oss.driver.api.querybuilder.select.SelectFrom;
import com.datastax.oss.driver.api.querybuilder.select.Selector;
import com.datastax.oss.driver.api.querybuilder.term.Term;
import com.datastax.oss.driver.internal.querybuilder.ArithmeticOperator;
import com.datastax.oss.driver.internal.querybuilder.DefaultLiteral;
import com.datastax.oss.driver.internal.querybuilder.DefaultRaw;
import com.datastax.oss.driver.internal.querybuilder.relation.CustomIndexRelation;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultColumnComponentRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultColumnRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultTokenRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.relation.DefaultTupleRelationBuilder;
import com.datastax.oss.driver.internal.querybuilder.select.AllSelector;
import com.datastax.oss.driver.internal.querybuilder.select.BinaryArithmeticSelector;
import com.datastax.oss.driver.internal.querybuilder.select.CastSelector;
import com.datastax.oss.driver.internal.querybuilder.select.ColumnSelector;
import com.datastax.oss.driver.internal.querybuilder.select.CountAllSelector;
import com.datastax.oss.driver.internal.querybuilder.select.DefaultBindMarker;
import com.datastax.oss.driver.internal.querybuilder.select.DefaultSelect;
import com.datastax.oss.driver.internal.querybuilder.select.ElementSelector;
import com.datastax.oss.driver.internal.querybuilder.select.FieldSelector;
import com.datastax.oss.driver.internal.querybuilder.select.FunctionSelector;
import com.datastax.oss.driver.internal.querybuilder.select.ListSelector;
import com.datastax.oss.driver.internal.querybuilder.select.MapSelector;
import com.datastax.oss.driver.internal.querybuilder.select.OppositeSelector;
import com.datastax.oss.driver.internal.querybuilder.select.RangeSelector;
import com.datastax.oss.driver.internal.querybuilder.select.SetSelector;
import com.datastax.oss.driver.internal.querybuilder.select.TupleSelector;
import com.datastax.oss.driver.internal.querybuilder.term.BinaryArithmeticTerm;
import com.datastax.oss.driver.internal.querybuilder.term.CastTerm;
import com.datastax.oss.driver.internal.querybuilder.term.FunctionTerm;
import com.datastax.oss.driver.internal.querybuilder.term.OppositeTerm;
import com.datastax.oss.driver.internal.querybuilder.term.TupleTerm;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Map;

/** A Domain-Specific Language to build CQL queries using Java code. */
public interface QueryBuilderDsl {

  /** The identifier of the built-in {@code writetime} function. */
  CqlIdentifier WRITETIME = CqlIdentifier.fromCql("writetime");

  /** The identifier of the built-in {@code ttl} function. */
  CqlIdentifier TTL = CqlIdentifier.fromCql("ttl");

  /** Starts a SELECT query for a qualified table. */
  static SelectFrom selectFrom(CqlIdentifier keyspace, CqlIdentifier table) {
    return new DefaultSelect(keyspace, table);
  }

  /**
   * Shortcut for {@link #selectFrom(CqlIdentifier, CqlIdentifier)
   * selectFrom(CqlIdentifier.fromCql(keyspace), CqlIdentifier.fromCql(table))}
   */
  static SelectFrom selectFrom(String keyspace, String table) {
    return selectFrom(CqlIdentifier.fromCql(keyspace), CqlIdentifier.fromCql(table));
  }

  /** Starts a SELECT query for an unqualified table. */
  static SelectFrom selectFrom(CqlIdentifier table) {
    return selectFrom(null, table);
  }

  /** Shortcut for {@link #selectFrom(CqlIdentifier) selectFrom(CqlIdentifier.fromCql(table))} */
  static SelectFrom selectFrom(String table) {
    return selectFrom(CqlIdentifier.fromCql(table));
  }

  /** Selects all columns, as in {@code SELECT *}. */
  static Selector getAll() {
    return AllSelector.INSTANCE;
  }

  /** Selects the count of all returned rows, as in {@code SELECT count(*)}. */
  static Selector getCountAll() {
    return new CountAllSelector();
  }

  /** Selects a particular column by its CQL identifier. */
  static Selector getColumn(CqlIdentifier columnId) {
    return new ColumnSelector(columnId);
  }

  /**
   * Shortcut for {@link QueryBuilderDsl#getColumn(CqlIdentifier)
   * getColumn(CqlIdentifier.fromCql(columnName))}
   */
  static Selector getColumn(String columnName) {
    return getColumn(CqlIdentifier.fromCql(columnName));
  }

  /**
   * Selects the sum of two arguments, as in {@code SELECT col1 + col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   */
  static Selector getSum(Selector left, Selector right) {
    return new BinaryArithmeticSelector(ArithmeticOperator.SUM, left, right);
  }

  /**
   * Selects the difference of two arguments, as in {@code SELECT col1 - col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   */
  static Selector getDifference(Selector left, Selector right) {
    return new BinaryArithmeticSelector(ArithmeticOperator.DIFFERENCE, left, right);
  }

  /**
   * Selects the product of two arguments, as in {@code SELECT col1 * col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>The arguments will be parenthesized if they are instances of {@link #getSum} or {@link
   * #getDifference}. If they are raw selectors, you might have to parenthesize them yourself.
   */
  static Selector getProduct(Selector left, Selector right) {
    return new BinaryArithmeticSelector(ArithmeticOperator.PRODUCT, left, right);
  }

  /**
   * Selects the quotient of two arguments, as in {@code SELECT col1 / col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>The arguments will be parenthesized if they are instances of {@link #getSum} or {@link
   * #getDifference}. If they are raw selectors, you might have to parenthesize them yourself.
   */
  static Selector getQuotient(Selector left, Selector right) {
    return new BinaryArithmeticSelector(ArithmeticOperator.QUOTIENT, left, right);
  }

  /**
   * Selects the remainder of two arguments, as in {@code SELECT col1 % col2}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>The arguments will be parenthesized if they are instances of {@link #getSum} or {@link
   * #getDifference}. If they are raw selectors, you might have to parenthesize them yourself.
   */
  static Selector getRemainder(Selector left, Selector right) {
    return new BinaryArithmeticSelector(ArithmeticOperator.REMAINDER, left, right);
  }

  /**
   * Selects the opposite of an argument, as in {@code SELECT -col1}.
   *
   * <p>This is available in Cassandra 4 and above.
   *
   * <p>The argument will be parenthesized if it is an instance of {@link #getSum} or {@link
   * #getDifference}. If it is a raw selector, you might have to parenthesize it yourself.
   */
  static Selector getOpposite(Selector argument) {
    return new OppositeSelector(argument);
  }

  /** Selects a field inside of a UDT column, as in {@code SELECT user.name}. */
  static Selector getField(Selector udt, CqlIdentifier fieldId) {
    return new FieldSelector(udt, fieldId);
  }

  /**
   * Shortcut for {@link #getField(Selector, CqlIdentifier) getUdtField(udt,
   * CqlIdentifier.fromCql(fieldName))}.
   */
  static Selector getField(Selector udt, String fieldName) {
    return getField(udt, CqlIdentifier.fromCql(fieldName));
  }

  /**
   * Shortcut to select a UDT field when the UDT is a simple column (as opposed to a more complex
   * selection, like a nested UDT).
   */
  static Selector getField(CqlIdentifier udtColumnId, CqlIdentifier fieldId) {
    return getField(getColumn(udtColumnId), fieldId);
  }

  /**
   * Shortcut for {@link #getField(CqlIdentifier, CqlIdentifier)
   * getField(CqlIdentifier.fromCql(udtColumnName), CqlIdentifier.fromCql(fieldName))}.
   */
  static Selector getField(String udtColumnName, String fieldName) {
    return getField(CqlIdentifier.fromCql(udtColumnName), CqlIdentifier.fromCql(fieldName));
  }

  /**
   * Selects an element in a collection column, as in {@code SELECT m['key']}.
   *
   * <p>As of Cassandra 4, this is only allowed for map and set columns.
   */
  static Selector getElement(Selector collection, Term index) {
    return new ElementSelector(collection, index);
  }

  /**
   * Shortcut for element selection when the target collection is a simple column.
   *
   * <p>In other words, this is the equivalent of {@link #getElement(Selector, Term)
   * getElement(getColumn(collectionId), index)}.
   */
  static Selector getElement(CqlIdentifier collectionId, Term index) {
    return getElement(getColumn(collectionId), index);
  }

  /**
   * Shortcut for {@link #getElement(CqlIdentifier, Term)
   * getElement(CqlIdentifier.fromCql(collectionName), index)}.
   */
  static Selector getElement(String collectionName, Term index) {
    return getElement(CqlIdentifier.fromCql(collectionName), index);
  }

  /**
   * Selects a slice in a collection column, as in {@code SELECT s[4..8]}.
   *
   * <p>As of Cassandra 4, this is only allowed for set and map columns. Those collections are
   * ordered, the elements (or keys in the case of a map), will be compared to the bounds for
   * inclusions. Either bound can be unspecified, but not both.
   *
   * @param left the left bound (inclusive). Can be {@code null} to indicate that the slice is only
   *     right-bound.
   * @param right the right bound (inclusive). Can be {@code null} to indicate that the slice is
   *     only left-bound.
   */
  static Selector getRange(Selector collection, Term left, Term right) {
    return new RangeSelector(collection, left, right);
  }

  /**
   * Shortcut for slice selection when the target collection is a simple column.
   *
   * <p>In other words, this is the equivalent of {@link #getRange(Selector, Term, Term)}
   * getRange(getColumn(collectionId), left, right)}.
   */
  static Selector getRange(CqlIdentifier collectionId, Term left, Term right) {
    return getRange(getColumn(collectionId), left, right);
  }

  /**
   * Shortcut for {@link #getRange(CqlIdentifier, Term, Term)
   * getRange(CqlIdentifier.fromCql(collectionName), left, right)}.
   */
  static Selector getRange(String collectionName, Term left, Term right) {
    return getRange(CqlIdentifier.fromCql(collectionName), left, right);
  }

  /**
   * Selects a group of elements as a list, as in {@code SELECT [a,b,c]}.
   *
   * <p>None of the selectors should be aliased (the query builder checks this at runtime), and they
   * should all produce the same data type (the query builder can't check this, so the query will
   * fail at execution time).
   *
   * @throws IllegalArgumentException if any of the selectors is aliased.
   */
  static Selector getListOf(Iterable<Selector> elementSelectors) {
    return new ListSelector(elementSelectors);
  }

  /** Var-arg equivalent of {@link #getListOf(Iterable)}. */
  static Selector getListOf(Selector... elementSelectors) {
    return getListOf(Arrays.asList(elementSelectors));
  }

  /**
   * Selects a group of elements as a set, as in {@code SELECT {a,b,c}}.
   *
   * <p>None of the selectors should be aliased (the query builder checks this at runtime), and they
   * should all produce the same data type (the query builder can't check this, so the query will
   * fail at execution time).
   *
   * @throws IllegalArgumentException if any of the selectors is aliased.
   */
  static Selector getSetOf(Iterable<Selector> elementSelectors) {
    return new SetSelector(elementSelectors);
  }

  /** Var-arg equivalent of {@link #getSetOf(Iterable)}. */
  static Selector getSetOf(Selector... elementSelectors) {
    return getSetOf(Arrays.asList(elementSelectors));
  }

  /**
   * Selects a group of elements as a tuple, as in {@code SELECT (a,b,c)}.
   *
   * <p>None of the selectors should be aliased (the query builder checks this at runtime).
   *
   * @throws IllegalArgumentException if any of the selectors is aliased.
   */
  static Selector getTupleOf(Iterable<Selector> elementSelectors) {
    return new TupleSelector(elementSelectors);
  }

  /** Var-arg equivalent of {@link #getTupleOf(Iterable)}. */
  static Selector getTupleOf(Selector... elementSelectors) {
    return getTupleOf(Arrays.asList(elementSelectors));
  }

  /**
   * Selects a group of elements as a map, as in {@code SELECT {a:b,c:d}}.
   *
   * <p>None of the selectors should be aliased (the query builder checks this at runtime). In
   * addition, all key selectors should produce the same type, and all value selectors as well (the
   * key and value types can be different); the query builder can't check this, so the query will
   * fail at execution time if the types are not uniform.
   *
   * <p>Note that Cassandra often has trouble inferring the exact map type. This will manifest as
   * the error message:
   *
   * <pre>
   *   Cannot infer type for term xxx in selection clause (try using a cast to force a type)
   * </pre>
   *
   * If you run into this, consider providing the types explicitly with {@link #getMapOf(Map,
   * DataType, DataType)}.
   *
   * @throws IllegalArgumentException if any of the selectors is aliased.
   */
  static Selector getMapOf(Map<Selector, Selector> elementSelectors) {
    return getMapOf(elementSelectors, null, null);
  }

  /**
   * Selects a group of elements as a map and force the resulting map type, as in {@code SELECT
   * (map<int,text>){a:b,c:d}}.
   *
   * <p>Use the constants and static methods in {@link DataTypes} to create the data types.
   *
   * @see #getMapOf(Map)
   */
  static Selector getMapOf(
      Map<Selector, Selector> elementSelectors, DataType keyType, DataType valueType) {
    return new MapSelector(elementSelectors, keyType, valueType);
  }

  /**
   * Forces a selector to a particular type, as in {@code SELECT (int)a}.
   *
   * <p>Use the constants and static methods in {@link DataTypes} to create the data type.
   */
  static Selector getCast(Selector selector, DataType targetType) {
    return new CastSelector(selector, targetType);
  }

  /**
   * Selects the result of a function call, as is {@code SELECT f(a,b)}
   *
   * <p>None of the arguments should be aliased (the query builder checks this at runtime).
   *
   * @throws IllegalArgumentException if any of the selectors is aliased.
   */
  static Selector getFunction(CqlIdentifier functionId, Iterable<Selector> arguments) {
    return new FunctionSelector(null, functionId, arguments);
  }

  /** Var-arg equivalent of {@link #getFunction(CqlIdentifier, Iterable)}. */
  static Selector getFunction(CqlIdentifier functionId, Selector... arguments) {
    return getFunction(functionId, Arrays.asList(arguments));
  }

  /**
   * Shortcut for {@link #getFunction(CqlIdentifier, Iterable)
   * getFunction(CqlIdentifier.fromCql(functionName), arguments)}.
   */
  static Selector getFunction(String functionName, Iterable<Selector> arguments) {
    return getFunction(CqlIdentifier.fromCql(functionName), arguments);
  }

  /** Var-arg equivalent of {@link #getFunction(String, Iterable)}. */
  static Selector getFunction(String functionName, Selector... arguments) {
    return getFunction(functionName, Arrays.asList(arguments));
  }

  /**
   * Selects the result of a function call, as is {@code SELECT ks.f(a,b)}
   *
   * <p>None of the arguments should be aliased (the query builder checks this at runtime).
   *
   * @throws IllegalArgumentException if any of the selectors is aliased.
   */
  static Selector getFunction(
      CqlIdentifier keyspaceId, CqlIdentifier functionId, Iterable<Selector> arguments) {
    return new FunctionSelector(keyspaceId, functionId, arguments);
  }

  /** Var-arg equivalent of {@link #getFunction(CqlIdentifier, CqlIdentifier, Iterable)}. */
  static Selector getFunction(
      CqlIdentifier keyspaceId, CqlIdentifier functionId, Selector... arguments) {
    return getFunction(keyspaceId, functionId, Arrays.asList(arguments));
  }

  /**
   * Shortcut for {@link #getFunction(CqlIdentifier, CqlIdentifier, Iterable)}
   * getFunction(CqlIdentifier.fromCql(functionName), arguments)}.
   */
  static Selector getFunction(
      String keyspaceName, String functionName, Iterable<Selector> arguments) {
    return getFunction(
        CqlIdentifier.fromCql(keyspaceName), CqlIdentifier.fromCql(functionName), arguments);
  }

  /** Var-arg equivalent of {@link #getFunction(String, String, Iterable)}. */
  static Selector getFunction(String keyspaceName, String functionName, Selector... arguments) {
    return getFunction(keyspaceName, functionName, Arrays.asList(arguments));
  }

  /**
   * Shortcut to select the result of the built-in {@code writetime} function, as in {@code SELECT
   * writetime(c)}.
   */
  static Selector getWriteTime(CqlIdentifier columnId) {
    return getFunction(WRITETIME, getColumn(columnId));
  }

  /**
   * Shortcut for {@link #getWriteTime(CqlIdentifier)
   * getWriteTime(CqlIdentifier.fromCql(columnName))}.
   */
  static Selector getWriteTime(String columnName) {
    return getWriteTime(CqlIdentifier.fromCql(columnName));
  }

  /**
   * Shortcut to select the result of the built-in {@code ttl} function, as in {@code SELECT
   * ttl(c)}.
   */
  static Selector getTtl(CqlIdentifier columnId) {
    return getFunction(TTL, getColumn(columnId));
  }

  /** Shortcut for {@link #getTtl(CqlIdentifier) getTtl(CqlIdentifier.fromCql(columnName))}. */
  static Selector getTtl(String columnName) {
    return getTtl(CqlIdentifier.fromCql(columnName));
  }

  /**
   * Builds a relation testing a column.
   *
   * <p>This call must be chained with an operator, for example:
   *
   * <pre>{@code
   * selectFrom("foo").getAll().where(isColumn("k").eq(bindMarker()));
   * }</pre>
   */
  static ColumnRelationBuilder isColumn(CqlIdentifier id) {
    return new DefaultColumnRelationBuilder(id);
  }

  /**
   * Shortcut for {@link QueryBuilderDsl#isColumn(CqlIdentifier)
   * isColumn(CqlIdentifier.fromCql(name))}
   */
  static ColumnRelationBuilder isColumn(String name) {
    return isColumn(CqlIdentifier.fromCql(name));
  }

  /**
   * Builds a relation testing a qualified component of collection (as of Cassandra 4, this can only
   * be used for map values).
   */
  static ColumnComponentRelationBuilder isColumnComponent(CqlIdentifier columnId, Term index) {
    return new DefaultColumnComponentRelationBuilder(columnId, index);
  }

  /**
   * Shortcut for {@link QueryBuilderDsl#isColumnComponent(CqlIdentifier, Term)
   * isColumnComponent(CqlIdentifier.fromCql(columnName), index)}
   */
  static ColumnComponentRelationBuilder isColumnComponent(String columnName, Term index) {
    return isColumnComponent(CqlIdentifier.fromCql(columnName), index);
  }

  /** Builds a relation testing a token generated from a set of columns. */
  static TokenRelationBuilder isTokenFromIds(Iterable<CqlIdentifier> identifiers) {
    return new DefaultTokenRelationBuilder(identifiers);
  }

  /** Var-arg equivalent of {@link QueryBuilderDsl#isTokenFromIds(Iterable)}. */
  static TokenRelationBuilder isToken(CqlIdentifier... identifiers) {
    return isTokenFromIds(Arrays.asList(identifiers));
  }

  /**
   * Equivalent of {@link QueryBuilderDsl#isTokenFromIds(Iterable)} with raw strings; the names are
   * converted with {@link CqlIdentifier#fromCql(String)}.
   */
  static TokenRelationBuilder isToken(Iterable<String> names) {
    return isTokenFromIds(Iterables.transform(names, CqlIdentifier::fromCql));
  }

  /** Var-arg equivalent of {@link #isToken(Iterable)}. */
  static TokenRelationBuilder isToken(String... names) {
    return isToken(Arrays.asList(names));
  }

  /** Builds a relation testing a set of columns, as in {@code WHERE (c1, c2, c3) IN ...}. */
  static TupleRelationBuilder isTupleOfIds(Iterable<CqlIdentifier> identifiers) {
    return new DefaultTupleRelationBuilder(identifiers);
  }

  /** Var-arg equivalent of {@link #isTupleOfIds(Iterable)}. */
  static TupleRelationBuilder isTuple(CqlIdentifier... identifiers) {
    return isTupleOfIds(Arrays.asList(identifiers));
  }

  /**
   * Equivalent of {@link #isTupleOfIds(Iterable)} with raw strings; the names are converted with
   * {@link CqlIdentifier#fromCql(String)}.
   */
  static TupleRelationBuilder isTuple(Iterable<String> names) {
    return isTupleOfIds(Iterables.transform(names, CqlIdentifier::fromCql));
  }

  /** Var-arg equivalent of {@link #isTuple(Iterable)}. */
  static TupleRelationBuilder isTuple(String... names) {
    return isTuple(Arrays.asList(names));
  }

  /** Builds a relation on a custom index. */
  static Relation isCustomIndex(CqlIdentifier indexId, Term expression) {
    return new CustomIndexRelation(indexId, expression);
  }

  /**
   * Shortcut for {@link #isCustomIndex(CqlIdentifier, Term)
   * isCustomIndex(CqlIdentifier.fromCql(indexName), expression)}
   */
  static Relation isCustomIndex(String indexName, Term expression) {
    return isCustomIndex(CqlIdentifier.fromCql(indexName), expression);
  }

  /**
   * An ordered set of anonymous terms, as in {@code WHERE (a, b) = (1, 2)} (on the right-hand
   * side).
   *
   * <p>For example, this can be used for the right-hand side of {@link
   * QueryBuilderDsl#isTuple(String...)}.
   */
  static Term tuple(Iterable<? extends Term> components) {
    return new TupleTerm(components);
  }

  /** Var-arg equivalent of {@link #tuple(Iterable)}. */
  static Term tuple(Term... components) {
    return tuple(Arrays.asList(components));
  }

  /** The sum of two terms, as in {@code WHERE k = left + right}. */
  static Term sum(Term left, Term right) {
    return new BinaryArithmeticTerm(ArithmeticOperator.SUM, left, right);
  }

  /** The difference of two terms, as in {@code WHERE k = left - right}. */
  static Term difference(Term left, Term right) {
    return new BinaryArithmeticTerm(ArithmeticOperator.DIFFERENCE, left, right);
  }

  /** The product of two terms, as in {@code WHERE k = left * right}. */
  static Term product(Term left, Term right) {
    return new BinaryArithmeticTerm(ArithmeticOperator.PRODUCT, left, right);
  }

  /** The quotient of two terms, as in {@code WHERE k = left / right}. */
  static Term quotient(Term left, Term right) {
    return new BinaryArithmeticTerm(ArithmeticOperator.QUOTIENT, left, right);
  }

  /** The remainder of two terms, as in {@code WHERE k = left % right}. */
  static Term remainder(Term left, Term right) {
    return new BinaryArithmeticTerm(ArithmeticOperator.REMAINDER, left, right);
  }

  /** The opposite of a term, as in {@code WHERE k = -argument}. */
  static Term opposite(Term argument) {
    return new OppositeTerm(argument);
  }

  /** A function call as a term, as in {@code WHERE = f(arguments)}. */
  static Term function(CqlIdentifier functionId, Iterable<Term> arguments) {
    return function(null, functionId, arguments);
  }

  /** Var-arg equivalent of {@link #function(CqlIdentifier, Iterable)}. */
  static Term function(CqlIdentifier functionId, Term... arguments) {
    return function(functionId, Arrays.asList(arguments));
  }

  /**
   * Shortcut for {@link #function(CqlIdentifier, Iterable)
   * function(CqlIdentifier.fromCql(functionName), arguments)}.
   */
  static Term function(String functionName, Iterable<Term> arguments) {
    return function(CqlIdentifier.fromCql(functionName), arguments);
  }

  /**
   * Shortcut for {@link #function(CqlIdentifier, Term...)
   * function(CqlIdentifier.fromCql(functionName), arguments)}.
   */
  static Term function(String functionName, Term... arguments) {
    return function(CqlIdentifier.fromCql(functionName), arguments);
  }

  /** A function call as a term, as in {@code WHERE = ks.f(arguments)}. */
  static Term function(
      CqlIdentifier keyspaceId, CqlIdentifier functionId, Iterable<Term> arguments) {
    return new FunctionTerm(keyspaceId, functionId, arguments);
  }

  /** Var-arg equivalent of {@link #function(CqlIdentifier, CqlIdentifier, Iterable)}. */
  static Term function(CqlIdentifier keyspaceId, CqlIdentifier functionId, Term... arguments) {
    return function(keyspaceId, functionId, Arrays.asList(arguments));
  }

  /**
   * Shortcut for {@link #function(CqlIdentifier, CqlIdentifier, Iterable)
   * function(CqlIdentifier.fromCql(keyspaceName), CqlIdentifier.fromCql(functionName), arguments)}.
   */
  static Term function(String keyspaceName, String functionName, Iterable<Term> arguments) {
    return function(
        CqlIdentifier.fromCql(keyspaceName), CqlIdentifier.fromCql(functionName), arguments);
  }

  /**
   * Shortcut for {@link #function(CqlIdentifier, CqlIdentifier, Term...)
   * function(CqlIdentifier.fromCql(keyspaceName), CqlIdentifier.fromCql(functionName), arguments)}.
   */
  static Term function(String keyspaceName, String functionName, Term... arguments) {
    return function(
        CqlIdentifier.fromCql(keyspaceName), CqlIdentifier.fromCql(functionName), arguments);
  }

  /**
   * Casts a term to a particular type, as in {@code WHERE k = (int)a}.
   *
   * <p>Use the constants and static methods in {@link DataTypes} to create the data type.
   */
  static Term cast(Term term, DataType targetType) {
    return new CastTerm(term, targetType);
  }

  /**
   * A literal term, as in {@code WHERE k = 1}.
   *
   * <p>This method can process any type for which there is a default Java to CQL mapping, namely:
   * primitive types ({@code Integer=>int, Long=>bigint, String=>text, etc.}), and collections,
   * tuples, and user defined types thereof.
   *
   * <p>A null argument will be rendered as {@code NULL}.
   *
   * <p>For custom mappings, use {@link #literal(Object, CodecRegistry)} or {@link #literal(Object,
   * TypeCodec)}.
   *
   * @throws CodecNotFoundException if there is no default CQL mapping for the Java type of {@code
   *     value}.
   */
  static Literal literal(Object value) {
    return literal(value, CodecRegistry.DEFAULT);
  }

  /**
   * A literal term, as in {@code WHERE k = 1}.
   *
   * <p>This is an alternative to {@link #literal(Object)} for custom type mappings. The provided
   * registry should contain a codec that can format the value. Typically, this will be your
   * session's registry, which is accessible via {@code session.getContext().codecRegistry()}.
   *
   * @see DriverContext#codecRegistry()
   * @throws CodecNotFoundException if {@code codecRegistry} does not contain any codec that can
   *     handle {@code value}.
   */
  static Literal literal(Object value, CodecRegistry codecRegistry) {
    return literal(value, (value == null) ? null : codecRegistry.codecFor(value));
  }

  /**
   * A literal term, as in {@code WHERE k = 1}.
   *
   * <p>This is an alternative to {@link #literal(Object)} for custom type mappings. The value will
   * be turned into a string with {@link TypeCodec#format(Object)}, and inlined in the query.
   */
  static <T> Literal literal(T value, TypeCodec<T> codec) {
    return new DefaultLiteral<>(value, codec);
  }

  /**
   * A raw CQL snippet.
   *
   * <p>The contents will be appended to the query as-is, without any syntax checking or escaping.
   * This method should be used with caution, as it's possible to generate invalid CQL that will
   * fail at execution time; on the other hand, it can be used as a workaround to handle new CQL
   * features that are not yet covered by the query builder.
   */
  static Raw raw(String raw) {
    return new DefaultRaw(raw);
  }

  /** Creates an anonymous bind marker, which appears as {@code ?} in the generated CQL. */
  static BindMarker bindMarker() {
    return bindMarker((CqlIdentifier) null);
  }

  /** Creates a named bind marker, which appears as {@code :id} in the generated CQL. */
  static BindMarker bindMarker(CqlIdentifier id) {
    return new DefaultBindMarker(id);
  }

  /** Shortcut for {@link #bindMarker(CqlIdentifier) bindMarker(CqlIdentifier.fromCql(name))} */
  static BindMarker bindMarker(String name) {
    return bindMarker(CqlIdentifier.fromCql(name));
  }
}
