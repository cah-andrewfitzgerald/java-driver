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
package com.datastax.oss.driver.api.querybuilder.term;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.CqlSnippet;
import com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl;
import com.datastax.oss.driver.api.querybuilder.relation.ArithmeticRelationBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Selector;

/**
 * A simple expression that doesn't reference columns.
 *
 * <p>For example, it can be used:
 *
 * <ul>
 *   <li>for the indices in a {@link Selector#range(CqlIdentifier, Term, Term) range selection};
 *   <li>as the right operand of a {@link ArithmeticRelationBuilder#isEqualTo(Term) relation}.
 * </ul>
 *
 * To build instances of this type, use the factory methods in {@link QueryBuilderDsl}, such as
 * {@link QueryBuilderDsl#literal(Object) literal}, {@link QueryBuilderDsl#tuple(Iterable) tuple},
 * etc.
 */
public interface Term extends CqlSnippet {}
