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

import com.datastax.oss.driver.api.querybuilder.term.Term;
import com.datastax.oss.driver.internal.querybuilder.ArithmeticOperator;
import com.google.common.base.Preconditions;
import java.util.Objects;

public class BinaryArithmeticTerm extends ArithmeticTerm {

  private final Term left;
  private final Term right;

  public BinaryArithmeticTerm(ArithmeticOperator operator, Term left, Term right) {
    super(operator);
    Preconditions.checkNotNull(left);
    Preconditions.checkNotNull(right);
    this.left = left;
    this.right = right;
  }

  @Override
  public void appendTo(StringBuilder builder) {
    appendAndMaybeParenthesize(operator.getPrecedenceLeft(), left, builder);
    builder.append(operator.getSymbol());
    appendAndMaybeParenthesize(operator.getPrecedenceRight(), right, builder);
  }

  public Term getLeft() {
    return left;
  }

  public Term getRight() {
    return right;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof BinaryArithmeticTerm) {
      BinaryArithmeticTerm that = (BinaryArithmeticTerm) other;
      return this.operator.equals(that.operator)
          && this.left.equals(that.left)
          && this.right.equals(that.right);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(operator, left, right);
  }
}