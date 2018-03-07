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
package com.datastax.oss.driver.internal.querybuilder.term;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.term.Term;
import com.google.common.base.Preconditions;

public class FunctionTerm implements Term {

  private final CqlIdentifier keyspaceId;
  private final CqlIdentifier functionId;
  private final Iterable<Term> arguments;

  public FunctionTerm(
      CqlIdentifier keyspaceId, CqlIdentifier functionId, Iterable<Term> arguments) {
    Preconditions.checkNotNull(functionId);
    Preconditions.checkNotNull(arguments);
    this.keyspaceId = keyspaceId;
    this.functionId = functionId;
    this.arguments = arguments;
  }

  @Override
  public String asCql(boolean pretty) {
    StringBuilder builder = new StringBuilder();
    if (keyspaceId != null) {
      builder.append(keyspaceId.asCql(pretty)).append('.');
    }
    builder.append(functionId.asCql(pretty)).append('(');
    boolean first = true;
    for (Term argument : arguments) {
      if (first) {
        first = false;
      } else {
        builder.append(",");
      }
      builder.append(argument.asCql(pretty));
    }
    return builder.append(")").toString();
  }

  public CqlIdentifier getKeyspaceId() {
    return keyspaceId;
  }

  public CqlIdentifier getFunctionId() {
    return functionId;
  }

  public Iterable<Term> getArguments() {
    return arguments;
  }
}
