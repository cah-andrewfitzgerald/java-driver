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
import java.util.Arrays;

public interface ColumnRelationBuilder extends ArithmeticRelationBuilder {

  /** Builds a LIKE relation for the column. */
  default Relation like(Term term) {
    return build(" LIKE ", term);
  }

  /** Builds an IS NOT NULL relation for the column. */
  default Relation notNull() {
    return build(" IS NOT NULL", null);
  }

  /**
   * Builds an IN relation for the column, where the whole set of possible values is a bound
   * variable, as in {@code IN ?}.
   */
  default Relation in(BindMarker bindMarker) {
    return build(" IN ", bindMarker);
  }

  /**
   * Builds an IN relation for the column, where the arguments are the possible values, as in {@code
   * IN (term1, term2...)}.
   */
  default Relation in(Iterable<Term> alternatives) {
    return build(" IN ", QueryBuilderDsl.tuple(alternatives));
  }

  /** Var-arg equivalent of {@link #in(Iterable)} . */
  default Relation in(Term... alternatives) {
    return in(Arrays.asList(alternatives));
  }

  /** Builds a CONTAINS relation for the column. */
  default Relation contains(Term term) {
    return build(" CONTAINS ", term);
  }

  /** Builds a CONTAINS KEY relation for the column. */
  default Relation containsKey(Term term) {
    return build(" CONTAINS KEY ", term);
  }
}
