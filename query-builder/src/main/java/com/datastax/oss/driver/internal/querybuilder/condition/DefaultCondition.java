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
import com.datastax.oss.driver.api.querybuilder.term.Term;
import com.datastax.oss.driver.internal.querybuilder.relation.LeftHandSide;

public class DefaultCondition implements Condition {

  private final LeftHandSide leftHandSide;
  private final String operator;
  private final Term rightHandSide;

  public DefaultCondition(LeftHandSide leftHandSide, String operator, Term rightHandSide) {
    this.leftHandSide = leftHandSide;
    this.operator = operator;
    this.rightHandSide = rightHandSide;
  }

  @Override
  public void appendTo(StringBuilder builder) {
    leftHandSide.appendTo(builder);
    builder.append(operator);
    if (rightHandSide != null) {
      rightHandSide.appendTo(builder);
    }
  }

  public LeftHandSide getLeftHandSide() {
    return leftHandSide;
  }

  public String getOperator() {
    return operator;
  }

  public Term getRightHandSide() {
    return rightHandSide;
  }
}
