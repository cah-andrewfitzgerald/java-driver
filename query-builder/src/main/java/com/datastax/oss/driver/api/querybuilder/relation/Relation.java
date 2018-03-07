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

import com.datastax.oss.driver.api.querybuilder.CqlSnippet;
import com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl;
import com.datastax.oss.driver.api.querybuilder.select.CanAddClause;

/**
 * A relation in a WHERE clause.
 *
 * <p>To build instances of this type, use the {@code isXxx} factory methods in {@link
 * QueryBuilderDsl}, such as {@link QueryBuilderDsl#isColumn(String) isColumn}, {@link
 * QueryBuilderDsl#isToken(String...) isToken}, etc.
 *
 * <p>They are used as arguments to the {@link CanAddClause#where(Iterable) where} method, for
 * example:
 *
 * <pre>{@code
 * selectFrom("foo").all().where(isColumn("k").eq(literal(1)))
 * // SELECT * FROM foo WHERE k=1
 * }</pre>
 */
public interface Relation extends CqlSnippet {}
