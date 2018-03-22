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
import com.datastax.oss.driver.api.querybuilder.delete.DeleteSelection;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.oss.driver.api.querybuilder.select.SelectFrom;
import com.datastax.oss.driver.api.querybuilder.term.Term;
import com.datastax.oss.driver.internal.querybuilder.ArithmeticOperator;
import com.datastax.oss.driver.internal.querybuilder.DefaultLiteral;
import com.datastax.oss.driver.internal.querybuilder.DefaultRaw;
import com.datastax.oss.driver.internal.querybuilder.delete.DefaultDelete;
import com.datastax.oss.driver.internal.querybuilder.select.DefaultBindMarker;
import com.datastax.oss.driver.internal.querybuilder.select.DefaultSelect;
import com.datastax.oss.driver.internal.querybuilder.term.BinaryArithmeticTerm;
import com.datastax.oss.driver.internal.querybuilder.term.FunctionTerm;
import com.datastax.oss.driver.internal.querybuilder.term.OppositeTerm;
import com.datastax.oss.driver.internal.querybuilder.term.TupleTerm;
import com.datastax.oss.driver.internal.querybuilder.term.TypeHintTerm;
import java.util.Arrays;

/** A Domain-Specific Language to build CQL queries using Java code. */
public class QueryBuilderDsl {

  /** The identifier of the built-in {@code writetime} function. */
  public static CqlIdentifier WRITETIME = CqlIdentifier.fromCql("writetime");

  /** The identifier of the built-in {@code ttl} function. */
  public static CqlIdentifier TTL = CqlIdentifier.fromCql("ttl");

  /** Starts a SELECT query for a qualified table. */
  public static SelectFrom selectFrom(CqlIdentifier keyspace, CqlIdentifier table) {
    return new DefaultSelect(keyspace, table);
  }

  /**
   * Shortcut for {@link #selectFrom(CqlIdentifier, CqlIdentifier)
   * selectFrom(CqlIdentifier.fromCql(keyspace), CqlIdentifier.fromCql(table))}
   */
  public static SelectFrom selectFrom(String keyspace, String table) {
    return selectFrom(CqlIdentifier.fromCql(keyspace), CqlIdentifier.fromCql(table));
  }

  /** Starts a SELECT query for an unqualified table. */
  public static SelectFrom selectFrom(CqlIdentifier table) {
    return selectFrom(null, table);
  }

  /** Shortcut for {@link #selectFrom(CqlIdentifier) selectFrom(CqlIdentifier.fromCql(table))} */
  public static SelectFrom selectFrom(String table) {
    return selectFrom(CqlIdentifier.fromCql(table));
  }

  /** Starts a DELETE query for a qualified table. */
  public static DeleteSelection deleteFrom(CqlIdentifier keyspace, CqlIdentifier table) {
    return new DefaultDelete(keyspace, table);
  }

  /**
   * Shortcut for {@link #deleteFrom(CqlIdentifier, CqlIdentifier)
   * deleteFrom(CqlIdentifier.fromCql(keyspace), CqlIdentifier.fromCql(table))}
   */
  public static DeleteSelection deleteFrom(String keyspace, String table) {
    return deleteFrom(CqlIdentifier.fromCql(keyspace), CqlIdentifier.fromCql(table));
  }

  /** Starts a DELETE query for an unqualified table. */
  public static DeleteSelection deleteFrom(CqlIdentifier table) {
    return deleteFrom(null, table);
  }

  /** Shortcut for {@link #deleteFrom(CqlIdentifier) deleteFrom(CqlIdentifier.fromCql(table))} */
  public static DeleteSelection deleteFrom(String table) {
    return deleteFrom(CqlIdentifier.fromCql(table));
  }

  /**
   * An ordered set of anonymous terms, as in {@code WHERE (a, b) = (1, 2)} (on the right-hand
   * side).
   *
   * <p>For example, this can be used as the right operand of {@link Relation#columns(String...)}.
   */
  public static Term tuple(Iterable<? extends Term> components) {
    return new TupleTerm(components);
  }

  /** Var-arg equivalent of {@link #tuple(Iterable)}. */
  public static Term tuple(Term... components) {
    return tuple(Arrays.asList(components));
  }

  /** The sum of two terms, as in {@code WHERE k = left + right}. */
  public static Term add(Term left, Term right) {
    return new BinaryArithmeticTerm(ArithmeticOperator.SUM, left, right);
  }

  /** The difference of two terms, as in {@code WHERE k = left - right}. */
  public static Term subtract(Term left, Term right) {
    return new BinaryArithmeticTerm(ArithmeticOperator.DIFFERENCE, left, right);
  }

  /** The product of two terms, as in {@code WHERE k = left * right}. */
  public static Term multiply(Term left, Term right) {
    return new BinaryArithmeticTerm(ArithmeticOperator.PRODUCT, left, right);
  }

  /** The quotient of two terms, as in {@code WHERE k = left / right}. */
  public static Term divide(Term left, Term right) {
    return new BinaryArithmeticTerm(ArithmeticOperator.QUOTIENT, left, right);
  }

  /** The remainder of two terms, as in {@code WHERE k = left % right}. */
  public static Term remainder(Term left, Term right) {
    return new BinaryArithmeticTerm(ArithmeticOperator.REMAINDER, left, right);
  }

  /** The opposite of a term, as in {@code WHERE k = -argument}. */
  public static Term negate(Term argument) {
    return new OppositeTerm(argument);
  }

  /** A function call as a term, as in {@code WHERE = f(arguments)}. */
  public static Term function(CqlIdentifier functionId, Iterable<Term> arguments) {
    return function(null, functionId, arguments);
  }

  /** Var-arg equivalent of {@link #function(CqlIdentifier, Iterable)}. */
  public static Term function(CqlIdentifier functionId, Term... arguments) {
    return function(functionId, Arrays.asList(arguments));
  }

  /**
   * Shortcut for {@link #function(CqlIdentifier, Iterable)
   * function(CqlIdentifier.fromCql(functionName), arguments)}.
   */
  public static Term function(String functionName, Iterable<Term> arguments) {
    return function(CqlIdentifier.fromCql(functionName), arguments);
  }

  /**
   * Shortcut for {@link #function(CqlIdentifier, Term...)
   * function(CqlIdentifier.fromCql(functionName), arguments)}.
   */
  public static Term function(String functionName, Term... arguments) {
    return function(CqlIdentifier.fromCql(functionName), arguments);
  }

  /** A function call as a term, as in {@code WHERE = ks.f(arguments)}. */
  public static Term function(
      CqlIdentifier keyspaceId, CqlIdentifier functionId, Iterable<Term> arguments) {
    return new FunctionTerm(keyspaceId, functionId, arguments);
  }

  /** Var-arg equivalent of {@link #function(CqlIdentifier, CqlIdentifier, Iterable)}. */
  public static Term function(
      CqlIdentifier keyspaceId, CqlIdentifier functionId, Term... arguments) {
    return function(keyspaceId, functionId, Arrays.asList(arguments));
  }

  /**
   * Shortcut for {@link #function(CqlIdentifier, CqlIdentifier, Iterable)
   * function(CqlIdentifier.fromCql(keyspaceName), CqlIdentifier.fromCql(functionName), arguments)}.
   */
  public static Term function(String keyspaceName, String functionName, Iterable<Term> arguments) {
    return function(
        CqlIdentifier.fromCql(keyspaceName), CqlIdentifier.fromCql(functionName), arguments);
  }

  /**
   * Shortcut for {@link #function(CqlIdentifier, CqlIdentifier, Term...)
   * function(CqlIdentifier.fromCql(keyspaceName), CqlIdentifier.fromCql(functionName), arguments)}.
   */
  public static Term function(String keyspaceName, String functionName, Term... arguments) {
    return function(
        CqlIdentifier.fromCql(keyspaceName), CqlIdentifier.fromCql(functionName), arguments);
  }

  /**
   * Provides a type hint for an expression, as in {@code WHERE k = (double)1/3}.
   *
   * <p>Use the constants and public static methods in {@link DataTypes} to create the data type.
   */
  public static Term typeHint(Term term, DataType targetType) {
    return new TypeHintTerm(term, targetType);
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
  public static Literal literal(Object value) {
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
  public static Literal literal(Object value, CodecRegistry codecRegistry) {
    return literal(value, (value == null) ? null : codecRegistry.codecFor(value));
  }

  /**
   * A literal term, as in {@code WHERE k = 1}.
   *
   * <p>This is an alternative to {@link #literal(Object)} for custom type mappings. The value will
   * be turned into a string with {@link TypeCodec#format(Object)}, and inlined in the query.
   */
  public static <T> Literal literal(T value, TypeCodec<T> codec) {
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
  public static Raw raw(String raw) {
    return new DefaultRaw(raw);
  }

  /** Creates an anonymous bind marker, which appears as {@code ?} in the generated CQL. */
  public static BindMarker bindMarker() {
    return bindMarker((CqlIdentifier) null);
  }

  /** Creates a named bind marker, which appears as {@code :id} in the generated CQL. */
  public static BindMarker bindMarker(CqlIdentifier id) {
    return new DefaultBindMarker(id);
  }

  /** Shortcut for {@link #bindMarker(CqlIdentifier) bindMarker(CqlIdentifier.fromCql(name))} */
  public static BindMarker bindMarker(String name) {
    return bindMarker(CqlIdentifier.fromCql(name));
  }
}
