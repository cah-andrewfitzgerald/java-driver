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
package com.datastax.oss.driver.internal.querybuilder.condition;

import com.datastax.oss.driver.api.querybuilder.condition.Condition;
import com.datastax.oss.driver.api.querybuilder.condition.ConditionBuilder;
import com.datastax.oss.driver.api.querybuilder.condition.ConditionalStatement;
import com.datastax.oss.driver.api.querybuilder.term.Term;
import com.datastax.oss.driver.internal.querybuilder.relation.LeftHandSide;

public class DefaultConditionBuilder implements ConditionBuilder<Condition> {

  private final LeftHandSide leftHandSide;

  public DefaultConditionBuilder(LeftHandSide leftHandSide) {
    this.leftHandSide = leftHandSide;
  }

  @Override
  public Condition build(String operator, Term rightHandSide) {
    return new DefaultCondition(leftHandSide, operator, rightHandSide);
  }

  public static class Fluent<StatementT extends ConditionalStatement<StatementT>>
      implements ConditionBuilder<StatementT> {

    private final ConditionalStatement<StatementT> statement;
    private final ConditionBuilder<Condition> delegate;

    public Fluent(ConditionalStatement<StatementT> statement, LeftHandSide leftHandSide) {
      this.statement = statement;
      this.delegate = new DefaultConditionBuilder(leftHandSide);
    }

    @Override
    public StatementT build(String operator, Term rightHandSide) {
      return statement.if_(delegate.build(operator, rightHandSide));
    }
  }
}
