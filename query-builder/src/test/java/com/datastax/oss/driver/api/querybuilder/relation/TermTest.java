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

import static com.datastax.oss.driver.api.querybuilder.Assertions.assertThat;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.cast;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.difference;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.function;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.literal;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.opposite;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.product;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.raw;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.remainder;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.sum;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.ProtocolVersion;
import com.datastax.oss.driver.api.core.data.TupleValue;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.TupleType;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.internal.core.type.UserDefinedTypeBuilder;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.junit.Test;

public class TermTest {

  @Test
  public void should_generate_arithmetic_terms() {
    assertThat(sum(raw("a"), raw("b"))).hasCql("a+b");
    assertThat(sum(sum(raw("a"), raw("b")), sum(raw("c"), raw("d")))).hasCql("a+b+c+d");
    assertThat(difference(sum(raw("a"), raw("b")), sum(raw("c"), raw("d")))).hasCql("a+b-(c+d)");
    assertThat(difference(sum(raw("a"), raw("b")), difference(raw("c"), raw("d"))))
        .hasCql("a+b-(c-d)");
    assertThat(opposite(sum(raw("a"), raw("b")))).hasCql("-(a+b)");
    assertThat(opposite(difference(raw("a"), raw("b")))).hasCql("-(a-b)");
    assertThat(product(sum(raw("a"), raw("b")), sum(raw("c"), raw("d")))).hasCql("(a+b)*(c+d)");
    assertThat(remainder(product(raw("a"), raw("b")), product(raw("c"), raw("d"))))
        .hasCql("a*b%(c*d)");
    assertThat(remainder(product(raw("a"), raw("b")), remainder(raw("c"), raw("d"))))
        .hasCql("a*b%(c%d)");
  }

  @Test
  public void should_generate_function_terms() {
    assertThat(function("f")).hasCql("f()");
    assertThat(function("f", raw("a"), raw("b"))).hasCql("f(a,b)");
    assertThat(function("ks", "f", raw("a"), raw("b"))).hasCql("ks.f(a,b)");
  }

  @Test
  public void should_generate_cast_terms() {
    assertThat(cast(raw("1"), DataTypes.BIGINT)).hasCql("(bigint)1");
  }

  @Test
  public void should_generate_literal_terms() {
    assertThat(literal(1)).hasCql("1");
    assertThat(literal("foo")).hasCql("'foo'");
    assertThat(literal(ImmutableList.of(1, 2, 3))).hasCql("[1,2,3]");

    TupleType tupleType = DataTypes.tupleOf(DataTypes.INT, DataTypes.TEXT);
    TupleValue tupleValue = tupleType.newValue().setInt(0, 1).setString(1, "foo");
    assertThat(literal(tupleValue)).hasCql("(1,'foo')");

    UserDefinedType udtType =
        new UserDefinedTypeBuilder(CqlIdentifier.fromCql("ks"), CqlIdentifier.fromCql("user"))
            .withField(CqlIdentifier.fromCql("first_name"), DataTypes.TEXT)
            .withField(CqlIdentifier.fromCql("last_name"), DataTypes.TEXT)
            .build();
    UdtValue udtValue =
        udtType.newValue().setString("first_name", "Jane").setString("last_name", "Doe");
    assertThat(literal(udtValue)).hasCql("{first_name:'Jane',last_name:'Doe'}");

    assertThat(literal(Charsets.UTF_8, new CharsetCodec())).hasCql("'UTF-8'");
  }

  static class CharsetCodec implements TypeCodec<Charset> {

    @Override
    public String format(Charset value) {
      return "'" + value.name() + "'";
    }

    @Override
    public GenericType<Charset> getJavaType() {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public DataType getCqlType() {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public ByteBuffer encode(Charset value, ProtocolVersion protocolVersion) {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public Charset decode(ByteBuffer bytes, ProtocolVersion protocolVersion) {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public Charset parse(String value) {
      throw new UnsupportedOperationException("Not used in this test");
    }
  }
}
