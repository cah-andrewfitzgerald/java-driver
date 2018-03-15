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

import com.datastax.oss.driver.api.querybuilder.BindMarker;
import com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl;
import com.datastax.oss.driver.api.querybuilder.term.Term;

public interface TupleRelationBuilder extends ArithmeticRelationBuilder {

  /**
   * Builds an IN relation, as in {@code WHERE (c1,c2,c3) IN ...}.
   *
   * <p>{@link BindMarker Bind markers} can be used on the right-hand side:
   *
   * <ul>
   *   <li>the whole set of alternatives as one variable:
   *       <pre>{@code
   * isTuple("c1", "c2", "c3").in(bindMarker())
   * // WHERE (c1,c2,c3) IN ?
   * }</pre>
   *   <li>each alternative tuple as a variable, using {@link QueryBuilderDsl#tuple(Iterable)}:
   *       <pre>{@code
   * isTuple("c1", "c2", "c3").in(tuple(bindMarker(), bindMarker())
   * // WHERE (c1,c2,c3) IN (?,?)
   * }</pre>
   *   <li>each tuple element as a variable:
   *       <pre>{@code
   * isTuple("c1", "c2", "c3")
   *     .in(
   *         tuple(
   *             tuple(bindMarker(), bindMarker(), bindMarker()),
   *             tuple(bindMarker(), bindMarker(), bindMarker())))))
   * // WHERE (c1,c2,c3) IN ((?,?,?),(?,?,?))
   * }</pre>
   * </ul>
   *
   * Bind markers may be mixed with literal terms:
   *
   * <pre>{@code
   * isTuple("c1", "c2", "c3").in(tuple(bindMarker(), raw("(4,5,6)")))
   * // WHERE (c1,c2,c3) IN (?,(4,5,6))
   * }</pre>
   */
  default Relation in(Term rightHandSide) {
    return build(" IN ", rightHandSide);
  }
}
