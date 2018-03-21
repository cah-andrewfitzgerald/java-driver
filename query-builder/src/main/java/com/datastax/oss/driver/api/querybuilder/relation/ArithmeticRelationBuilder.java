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

import com.datastax.oss.driver.api.querybuilder.term.Term;

public interface ArithmeticRelationBuilder<ResultT> {

  /**
   * Builds an '=' relation with the given term.
   *
   * <p>Use one of the static factory method in {@link Term} to create the argument.
   */
  default ResultT eq(Term rightHandSide) {
    return build("=", rightHandSide);
  }

  /**
   * Builds a '<' relation with the given term.
   *
   * <p>Use one of the static factory method in {@link Term} to create the argument.
   */
  default ResultT lt(Term rightHandSide) {
    return build("<", rightHandSide);
  }

  /**
   * Builds a '<=' relation with the given term.
   *
   * <p>Use one of the static factory method in {@link Term} to create the argument.
   */
  default ResultT lte(Term rightHandSide) {
    return build("<=", rightHandSide);
  }

  /**
   * Builds a '>' relation with the given term.
   *
   * <p>Use one of the static factory method in {@link Term} to create the argument.
   */
  default ResultT gt(Term rightHandSide) {
    return build(">", rightHandSide);
  }

  /**
   * Builds a '>=' relation with the given term.
   *
   * <p>Use one of the static factory method in {@link Term} to create the argument.
   */
  default ResultT gte(Term rightHandSide) {
    return build(">=", rightHandSide);
  }

  /**
   * Builds a '!=' relation with the given term.
   *
   * <p>Use one of the static factory method in {@link Term} to create the argument.
   */
  default ResultT ne(Term rightHandSide) {
    return build("!=", rightHandSide);
  }

  ResultT build(String operator, Term rightHandSide);
}
