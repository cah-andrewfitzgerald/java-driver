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

/** A statement that is ready to accept relations in its WHERE clause. */
public interface CanAddRelation<SelfT> {

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
   * <p>To create the argument, use one of the {@code isXxx} factory methods in {@link
   * QueryBuilderDsl}, for example {@link QueryBuilderDsl#isColumn(CqlIdentifier) isColumn}.
   *
   * @see #where(Relation)
   */
  SelfT where(Iterable<Relation> additionalRelations);

  /** Var-arg equivalent of {@link #where(Iterable)}. */
  SelfT where(Relation... additionalRelations);
}
