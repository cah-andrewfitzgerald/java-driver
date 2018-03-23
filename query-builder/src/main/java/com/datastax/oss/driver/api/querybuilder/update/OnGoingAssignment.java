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
package com.datastax.oss.driver.api.querybuilder.update;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl;
import com.datastax.oss.driver.api.querybuilder.term.Term;
import java.util.Arrays;

public interface OnGoingAssignment {

  /**
   * Adds an assignment to this statement, as in {@code UPDATE foo SET v=1}.
   *
   * <p>To create the argument, use one of the factory methods in {@link Assignment}, for example
   * Assignment{@link #setColumn(CqlIdentifier, Term)}. This type also provides shortcuts to create
   * and add the assignment in one call, for example {@link #setColumn(CqlIdentifier, Term)}.
   *
   * <p>If you add multiple assignments as one, consider {@link #set(Iterable)} as a more efficient
   * alternative.
   */
  UpdateWithAssignments set(Assignment assignment);

  /**
   * Adds multiple assignments at once.
   *
   * <p>This is slightly more efficient than adding the assignments one by one (since the underlying
   * implementation of this object is immutable).
   *
   * <p>To create the argument, use one of the factory methods in {@link Assignment}, for example
   * Assignment{@link #setColumn(CqlIdentifier, Term)}.
   */
  UpdateWithAssignments set(Iterable<Assignment> additionalAssignments);

  /** Var-arg equivalent of {@link #set(Iterable)}. */
  default UpdateWithAssignments set(Assignment... assignments) {
    return set(Arrays.asList(assignments));
  }

  /**
   * Assigns a value to a column, as in {@code SET c=1}.
   *
   * <p>This is a shortcut for {@link #set(Assignment) set(Assignment.setColumn(columnId, value))}.
   *
   * @see Assignment#setColumn(CqlIdentifier, Term)
   */
  default UpdateWithAssignments setColumn(CqlIdentifier columnId, Term value) {
    return set(Assignment.setColumn(columnId, value));
  }

  /**
   * Shortcut for {@link #setColumn(CqlIdentifier, Term)
   * setColumn(CqlIdentifier.fromCql(columnName), value)}.
   *
   * @see Assignment#setColumn(String, Term)
   */
  default UpdateWithAssignments setColumn(String columnName, Term value) {
    return setColumn(CqlIdentifier.fromCql(columnName), value);
  }

  /**
   * Assigns a value to a field of a UDT, as in {@code SET address.zip=?}.
   *
   * <p>This is a shortcut for {@link #set(Assignment) set(Assignment.setField(columnId, fieldId,
   * value))}.
   *
   * @see Assignment#setField(CqlIdentifier, CqlIdentifier, Term)
   */
  default UpdateWithAssignments setField(
      CqlIdentifier columnId, CqlIdentifier fieldId, Term value) {
    return set(Assignment.setField(columnId, fieldId, value));
  }

  /**
   * Shortcut for {@link #setField(CqlIdentifier, CqlIdentifier, Term)
   * setField(CqlIdentifier.fromCql(columnName), CqlIdentifier.fromCql(fieldName), value)}.
   *
   * @see Assignment#setField(String, String, Term)
   */
  default UpdateWithAssignments setField(String columnName, String fieldName, Term value) {
    return setField(CqlIdentifier.fromCql(columnName), CqlIdentifier.fromCql(fieldName), value);
  }

  /**
   * Assigns a value to an entry in a map column, as in {@code SET map[?]=?}.
   *
   * <p>This is a shortcut for {@link #set(Assignment) set(Assignment.setMapValue(columnId, index,
   * value))}.
   *
   * @see Assignment#setMapValue(CqlIdentifier, Term, Term)
   */
  default UpdateWithAssignments setMapValue(CqlIdentifier columnId, Term index, Term value) {
    return set(Assignment.setMapValue(columnId, index, value));
  }

  /**
   * Shortcut for {@link #setMapValue(CqlIdentifier, Term, Term)
   * setMapValue(CqlIdentifier.fromCql(columnName), index, value)}.
   *
   * @see Assignment#setMapValue(String, Term, Term)
   */
  default UpdateWithAssignments setMapValue(String columnName, Term index, Term value) {
    return setMapValue(CqlIdentifier.fromCql(columnName), index, value);
  }

  /**
   * Increments a counter, as in {@code SET c+=?}.
   *
   * <p>This is a shortcut for {@link #set(Assignment) set(Assignment.increment(columnId, amount))}.
   *
   * @see Assignment#increment(CqlIdentifier, Term)
   */
  default UpdateWithAssignments increment(CqlIdentifier columnId, Term amount) {
    return set(Assignment.increment(columnId, amount));
  }

  /**
   * Shortcut for {@link #increment(CqlIdentifier, Term)
   * increment(CqlIdentifier.fromCql(columnName), amount)}
   *
   * @see Assignment#increment(String, Term)
   */
  default UpdateWithAssignments increment(String columnName, Term amount) {
    return increment(CqlIdentifier.fromCql(columnName), amount);
  }

  /**
   * Increments a counter by 1, as in {@code SET c+=1} .
   *
   * <p>This is a shortcut for {@link #increment(CqlIdentifier, Term)} increment(columnId,
   * QueryBuilderDsl.literal(1))}.
   *
   * @see Assignment#increment(CqlIdentifier)
   */
  default UpdateWithAssignments increment(CqlIdentifier columnId) {
    return increment(columnId, QueryBuilderDsl.literal(1));
  }

  /**
   * Shortcut for {@link #increment(CqlIdentifier) CqlIdentifier.fromCql(columnName)}.
   *
   * @see Assignment#increment(CqlIdentifier)
   */
  default UpdateWithAssignments increment(String columnName) {
    return increment(CqlIdentifier.fromCql(columnName));
  }

  /**
   * Decrements a counter, as in {@code SET c-=?}.
   *
   * <p>This is a shortcut for {@link #set(Assignment) set(Assignment.decrement(columnId, amount))}.
   *
   * @see Assignment#decrement(CqlIdentifier, Term)
   */
  default UpdateWithAssignments decrement(CqlIdentifier columnId, Term amount) {
    return set(Assignment.decrement(columnId, amount));
  }

  /**
   * Shortcut for {@link #decrement(CqlIdentifier, Term)
   * decrement(CqlIdentifier.fromCql(columnName), amount)}
   *
   * @see Assignment#decrement(String, Term)
   */
  default UpdateWithAssignments decrement(String columnName, Term amount) {
    return decrement(CqlIdentifier.fromCql(columnName), amount);
  }

  /**
   * Decrements a counter by 1, as in {@code SET c-=1}.
   *
   * <p>This is a shortcut for {@link #decrement(CqlIdentifier, Term)} decrement(columnId, 1)}.
   *
   * @see Assignment#decrement(CqlIdentifier)
   */
  default UpdateWithAssignments decrement(CqlIdentifier columnId) {
    return decrement(columnId, QueryBuilderDsl.literal(1));
  }

  /**
   * Shortcut for {@link #decrement(CqlIdentifier) CqlIdentifier.fromCql(columnName)}.
   *
   * @see Assignment#decrement(String)
   */
  default UpdateWithAssignments decrement(String columnName) {
    return decrement(CqlIdentifier.fromCql(columnName));
  }

  /**
   * Appends to a collection column, as in {@code SET l+=?}.
   *
   * <p>The term must be a collection of the same type as the column.
   *
   * <p>This is a shortcut for {@link #set(Assignment) set(Assignment.append(columnId, suffix))}.
   *
   * @see Assignment#append(CqlIdentifier, Term)
   */
  default UpdateWithAssignments append(CqlIdentifier columnId, Term suffix) {
    return set(Assignment.append(columnId, suffix));
  }

  /**
   * Shortcut for {@link #append(CqlIdentifier, Term) append(CqlIdentifier.fromCql(columnName),
   * suffix)}.
   *
   * @see Assignment#append(String, Term)
   */
  default UpdateWithAssignments append(String columnName, Term suffix) {
    return append(CqlIdentifier.fromCql(columnName), suffix);
  }

  /**
   * Appends a single element to a list column, as in {@code SET l+=[?]}.
   *
   * <p>The term must be of the same type as the column's elements.
   *
   * <p>This is a shortcut for {@link #set(Assignment) set(Assignment.appendListElement(columnId,
   * suffix))}.
   *
   * @see Assignment#appendListElement(CqlIdentifier, Term)
   */
  default UpdateWithAssignments appendListElement(CqlIdentifier columnId, Term suffix) {
    return set(Assignment.appendListElement(columnId, suffix));
  }

  /**
   * Shortcut for {@link #appendListElement(CqlIdentifier, Term)
   * appendListElement(CqlIdentifier.fromCql(columnName), suffix)}.
   *
   * @see Assignment#appendListElement(String, Term)
   */
  default UpdateWithAssignments appendListElement(String columnName, Term suffix) {
    return appendListElement(CqlIdentifier.fromCql(columnName), suffix);
  }

  /**
   * Appends a single element to a set column, as in {@code SET s+={?}}.
   *
   * <p>The term must be of the same type as the column's elements.
   *
   * <p>This is a shortcut for {@link #set(Assignment) set(Assignment.appendSetElement(columnId,
   * suffix))}.
   *
   * @see Assignment#appendSetElement(CqlIdentifier, Term)
   */
  default UpdateWithAssignments appendSetElement(CqlIdentifier columnId, Term suffix) {
    return set(Assignment.appendSetElement(columnId, suffix));
  }

  /**
   * Shortcut for {@link #appendSetElement(CqlIdentifier, Term)
   * appendSetElement(CqlIdentifier.fromCql(columnName), suffix)}.
   */
  default UpdateWithAssignments appendSetElement(String columnName, Term suffix) {
    return appendSetElement(CqlIdentifier.fromCql(columnName), suffix);
  }

  /**
   * Appends a single entry to a map column, as in {@code SET m+={?:?}}.
   *
   * <p>The terms must be of the same type as the column's keys and values respectively.
   *
   * <p>This is a shortcut for {@link #set(Assignment) set(Assignment.appendMapEntry(columnId, key,
   * value)}.
   *
   * @see Assignment#appendMapEntry(CqlIdentifier, Term, Term)
   */
  default UpdateWithAssignments appendMapEntry(CqlIdentifier columnId, Term key, Term value) {
    return set(Assignment.appendMapEntry(columnId, key, value));
  }

  /**
   * Shortcut for {@link #appendMapEntry(CqlIdentifier, Term, Term)
   * appendMapEntry(CqlIdentifier.fromCql(columnName), key, value)}.
   *
   * @see Assignment#appendMapEntry(String, Term, Term)
   */
  default UpdateWithAssignments appendMapEntry(String columnName, Term key, Term value) {
    return appendMapEntry(CqlIdentifier.fromCql(columnName), key, value);
  }

  /**
   * Prepends to a collection column, as in {@code SET l=[1,2,3]+l}.
   *
   * <p>The term must be a collection of the same type as the column.
   *
   * <p>This is a shortcut for {@link #set(Assignment) set(Assignment.prepend(columnId, prefix))}.
   *
   * @see Assignment#prepend(CqlIdentifier, Term)
   */
  default UpdateWithAssignments prepend(CqlIdentifier columnId, Term prefix) {
    return set(Assignment.prepend(columnId, prefix));
  }

  /**
   * Shortcut for {@link #prepend(CqlIdentifier, Term) prepend(CqlIdentifier.fromCql(columnName),
   * prefix)}.
   *
   * @see Assignment#prepend(String, Term)
   */
  default UpdateWithAssignments prepend(String columnName, Term prefix) {
    return prepend(CqlIdentifier.fromCql(columnName), prefix);
  }

  /**
   * Prepends a single element to a list column, as in {@code SET l=[?]+l}.
   *
   * <p>The term must be of the same type as the column's elements.
   *
   * <p>This is a shortcut for {@link #set(Assignment) set(Assignment.prependListElement(columnId,
   * suffix))}.
   *
   * @see Assignment#prependListElement(CqlIdentifier, Term)
   */
  default UpdateWithAssignments prependListElement(CqlIdentifier columnId, Term suffix) {
    return set(Assignment.prependListElement(columnId, suffix));
  }

  /**
   * Shortcut for {@link #prependListElement(CqlIdentifier, Term)
   * prependListElement(CqlIdentifier.fromCql(columnName), suffix)}.
   *
   * @see Assignment#prependListElement(String, Term)
   */
  default UpdateWithAssignments prependListElement(String columnName, Term suffix) {
    return prependListElement(CqlIdentifier.fromCql(columnName), suffix);
  }

  /**
   * Prepends a single element to a set column, as in {@code SET s={?}+s}.
   *
   * <p>The term must be of the same type as the column's elements.
   *
   * <p>This is a shortcut for {@link #set(Assignment) set(Assignment.prependSetElement(columnId,
   * suffix))}.
   *
   * @see Assignment#prependSetElement(CqlIdentifier, Term)
   */
  default UpdateWithAssignments prependSetElement(CqlIdentifier columnId, Term suffix) {
    return set(Assignment.prependSetElement(columnId, suffix));
  }

  /**
   * Shortcut for {@link #prependSetElement(CqlIdentifier, Term)
   * prependSetElement(CqlIdentifier.fromCql(columnName), suffix)}.
   *
   * @see Assignment#prependSetElement(String, Term)
   */
  default UpdateWithAssignments prependSetElement(String columnName, Term suffix) {
    return prependSetElement(CqlIdentifier.fromCql(columnName), suffix);
  }

  /**
   * Prepends a single entry to a map column, as in {@code SET m={?:?}+m}.
   *
   * <p>The terms must be of the same type as the column's keys and values respectively.
   *
   * <p>This is a shortcut for {@link #set(Assignment) set(Assignment.prependMapEntry(columnId, key,
   * value))}.
   *
   * @see Assignment#prependMapEntry(CqlIdentifier, Term, Term)
   */
  default UpdateWithAssignments prependMapEntry(CqlIdentifier columnId, Term key, Term value) {
    return set(Assignment.prependMapEntry(columnId, key, value));
  }

  /**
   * Shortcut for {@link #prependMapEntry(CqlIdentifier, Term, Term)
   * prependMapEntry(CqlIdentifier.fromCql(columnName), key, value)}.
   *
   * @see Assignment#prependMapEntry(String, Term, Term)
   */
  default UpdateWithAssignments prependMapEntry(String columnName, Term key, Term value) {
    return prependMapEntry(CqlIdentifier.fromCql(columnName), key, value);
  }
}
