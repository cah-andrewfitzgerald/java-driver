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
package com.datastax.oss.driver.internal.querybuilder.relation;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.querybuilder.relation.CanAddRelation;
import com.datastax.oss.driver.api.querybuilder.relation.ColumnComponentRelationBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.oss.driver.api.querybuilder.term.Term;

public class DefaultColumnComponentRelationBuilder
    implements ColumnComponentRelationBuilder<Relation> {

  private final CqlIdentifier columnId;
  private final Term index;

  public DefaultColumnComponentRelationBuilder(CqlIdentifier columnId, Term index) {
    this.columnId = columnId;
    this.index = index;
  }

  @Override
  public Relation build(String operator, Term rightHandSide) {
    return new DefaultRelation(
        new ColumnComponentLeftHandSide(columnId, index), operator, rightHandSide);
  }

  public static class Fluent<StatementT extends CanAddRelation<StatementT>>
      implements ColumnComponentRelationBuilder<StatementT> {

    private final CanAddRelation<StatementT> statement;
    private final ColumnComponentRelationBuilder<Relation> delegate;

    public Fluent(CanAddRelation<StatementT> statement, CqlIdentifier columnId, Term index) {
      this.statement = statement;
      this.delegate = new DefaultColumnComponentRelationBuilder(columnId, index);
    }

    @Override
    public StatementT build(String operator, Term rightHandSide) {
      return statement.where(delegate.build(operator, rightHandSide));
    }
  }
}
