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
import com.datastax.oss.driver.api.querybuilder.CqlSnippet;
import com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl;

/**
 * A selected element in a SELECT query.
 *
 * <p>To build instances of this type, use the {@code getXxx} factory methods in {@link
 * QueryBuilderDsl}, such as {@link QueryBuilderDsl#getColumn(CqlIdentifier) getColumn}, {@link
 * QueryBuilderDsl#getFunction(CqlIdentifier, Iterable) getFunction}, etc.
 *
 * <p>They are used as arguments to the {@link CanAddSelector#selectors(Iterable) selectors} method,
 * for example:
 *
 * <pre>{@code
 * selectFrom("foo").selectors(getColumn("bar"), getColumn("baz"))
 * // SELECT bar,baz FROM foo
 * }</pre>
 *
 * <p>There are also shortcuts in the fluent API when you build a statement, for example:
 *
 * <pre>{@code
 * selectFrom("foo").column("bar").column("baz")
 * // SELECT bar,baz FROM foo
 * }</pre>
 */
public interface Selector extends CqlSnippet {

  /** Aliases the selector, as in {@code SELECT count(*) AS total}. */
  Selector as(CqlIdentifier alias);

  /** Shortcut for {@link #as(CqlIdentifier) as(CqlIdentifier.fromCql(alias))} */
  default Selector as(String alias) {
    return as(CqlIdentifier.fromCql(alias));
  }

  /** @return null if the selector is not aliased. */
  CqlIdentifier getAlias();
}
