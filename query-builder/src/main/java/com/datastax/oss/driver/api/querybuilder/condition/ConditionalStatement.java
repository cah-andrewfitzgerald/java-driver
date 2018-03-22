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
package com.datastax.oss.driver.api.querybuilder.condition;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl;
import com.datastax.oss.driver.api.querybuilder.term.Term;
import com.datastax.oss.driver.internal.querybuilder.condition.DefaultConditionBuilder;
import com.datastax.oss.driver.internal.querybuilder.lhs.ColumnComponentLeftHandSide;
import com.datastax.oss.driver.internal.querybuilder.lhs.ColumnLeftHandSide;
import com.datastax.oss.driver.internal.querybuilder.lhs.FieldLeftHandSide;
import java.util.Arrays;

/**
 * A statement that can be applied conditionally, such as UPDATE or DELETE.
 *
 * <p>Not that this does not covert INSERT... IF NOT EXISTS, which is handled separately.
 */
public interface ConditionalStatement<SelfT extends ConditionalStatement<SelfT>> {

  /**
   * Adds an IF EXISTS condition.
   *
   * <p>If any column conditions were added before, they will be cleared.
   */
  SelfT ifExists();

  /**
   * Adds an IF condition. All conditions are logically joined with AND. If {@link #ifExists()} was
   * invoked on this statement before, it will get cancelled.
   *
   * <p>To create the argument, use one of the {@code ifXxx} factory methods in {@link
   * QueryBuilderDsl}, for example {@link QueryBuilderDsl#ifColumn(CqlIdentifier) isColumn}.
   *
   * <p>If you add multiple conditions as once, consider {@link #if_(Iterable)} as a more efficient
   * alternative.
   */
  SelfT if_(Condition condition);

  /**
   * Adds multiple IF conditions at once. All conditions are logically joined with AND. If {@link
   * #ifExists()} was invoked on this statement before, it will get cancelled.
   *
   * <p>This is slightly more efficient than adding the relations one by one (since the underlying
   * implementation of this object is immutable).
   *
   * <p>To create the arguments, use one of the {@code ifXxx} factory methods in {@link
   * QueryBuilderDsl}, for example {@link QueryBuilderDsl#ifColumn(CqlIdentifier) isColumn}.
   */
  SelfT if_(Iterable<Condition> conditions);

  /** Var-arg equivalent of {@link #if_(Iterable)}. */
  default SelfT if_(Condition... conditions) {
    return if_(Arrays.asList(conditions));
  }

  /**
   * Adds an IF condition on a simple column, as in {@code DELETE... IF k=1}.
   *
   * <p>This is the equivalent of creating a condition with {@link
   * QueryBuilderDsl#ifColumn(CqlIdentifier)} and passing it to {@link #if_(Condition)}.
   */
  default ConditionBuilder<SelfT> ifColumn(CqlIdentifier columnId) {
    return new DefaultConditionBuilder.Fluent<>(this, new ColumnLeftHandSide(columnId));
  }

  /**
   * Shortcut for {@link #ifColumn(CqlIdentifier) ifColumn(CqlIdentifier.fromCql(columnName))}.
   *
   * <p>This is the equivalent of creating a condition with {@link QueryBuilderDsl#ifColumn(String)}
   * and passing it to {@link #if_(Condition)}.
   */
  default ConditionBuilder<SelfT> ifColumn(String columnName) {
    return ifColumn(CqlIdentifier.fromCql(columnName));
  }

  /**
   * Adds an IF condition on a field in a UDT column for a conditional statement, as in {@code
   * DELETE... IF address.street='test'}.
   *
   * <p>This is the equivalent of creating a condition with {@link
   * QueryBuilderDsl#ifField(CqlIdentifier, CqlIdentifier)} and passing it to {@link
   * #if_(Condition)}.
   */
  default ConditionBuilder<SelfT> ifField(CqlIdentifier columnId, CqlIdentifier fieldId) {
    return new DefaultConditionBuilder.Fluent<>(this, new FieldLeftHandSide(columnId, fieldId));
  }

  /**
   * Shortcut for {@link #ifField(CqlIdentifier, CqlIdentifier)
   * ifField(CqlIdentifier.fromCql(columnName), CqlIdentifier.fromCql(fieldName))}.
   *
   * <p>This is the equivalent of creating a condition with {@link QueryBuilderDsl#ifField(String,
   * String)} and passing it to {@link #if_(Condition)}.
   */
  default ConditionBuilder<SelfT> ifField(String columnName, String fieldName) {
    return ifField(CqlIdentifier.fromCql(columnName), CqlIdentifier.fromCql(fieldName));
  }

  /**
   * Adds an IF condition on an element in a collection column for a conditional statement, as in
   * {@code DELETE... IF m[0]=1}.
   *
   * <p>This is the equivalent of creating a condition with {@link
   * QueryBuilderDsl#ifElement(CqlIdentifier, Term)} and passing it to {@link #if_(Condition)}.
   */
  default ConditionBuilder<SelfT> ifElement(CqlIdentifier columnId, Term index) {
    return new DefaultConditionBuilder.Fluent<>(
        this, new ColumnComponentLeftHandSide(columnId, index));
  }

  /**
   * Shortcut for {@link #ifElement(CqlIdentifier, Term)
   * ifElement(CqlIdentifier.fromCql(columnName), index)}.
   *
   * <p>This is the equivalent of creating a condition with {@link QueryBuilderDsl#ifElement(String,
   * Term)} and passing it to {@link #if_(Condition)}.
   */
  default ConditionBuilder<SelfT> ifElement(String columnName, Term index) {
    return ifElement(CqlIdentifier.fromCql(columnName), index);
  }

  /**
   * Adds a raw CQL snippet as a condition.
   *
   * <p>This is the equivalent of creating a condition with {@link QueryBuilderDsl#raw(String)} and
   * passing it to {@link #if_(Condition)}.
   *
   * <p>The contents will be appended to the query as-is, without any syntax checking or escaping.
   * This method should be used with caution, as it's possible to generate invalid CQL that will
   * fail at execution time; on the other hand, it can be used as a workaround to handle new CQL
   * features that are not yet covered by the query builder.
   *
   * @see QueryBuilderDsl#raw(String)
   */
  default SelfT ifRaw(String raw) {
    return if_(QueryBuilderDsl.raw(raw));
  }
}
