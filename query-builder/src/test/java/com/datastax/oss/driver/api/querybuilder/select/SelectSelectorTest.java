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

import static com.datastax.oss.driver.api.querybuilder.Assertions.assertThat;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getAll;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getColumn;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getElement;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getField;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getOpposite;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getProduct;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getSum;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.literal;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.raw;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.selectFrom;

import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.CharsetCodec;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

public class SelectSelectorTest {

  @Test
  public void should_generate_star_selector() {
    assertThat(selectFrom("foo").all()).hasCql("SELECT * FROM foo");
    assertThat(selectFrom("ks", "foo").all()).hasCql("SELECT * FROM ks.foo");
  }

  @Test
  public void should_remove_star_selector_if_other_selector_added() {
    assertThat(selectFrom("foo").all().column("bar")).hasCql("SELECT bar FROM foo");
  }

  @Test
  public void should_remove_other_selectors_if_star_selector_added() {
    assertThat(selectFrom("foo").column("bar").column("baz").all()).hasCql("SELECT * FROM foo");
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_fail_if_selector_list_contains_star_selector() {
    selectFrom("foo").selectors(getColumn("bar"), getAll(), raw("baz"));
  }

  @Test
  public void should_generate_count_all_selector() {
    assertThat(selectFrom("foo").countAll()).hasCql("SELECT count(*) FROM foo");
  }

  @Test
  public void should_generate_column_selectors() {
    assertThat(selectFrom("foo").column("bar")).hasCql("SELECT bar FROM foo");
    assertThat(selectFrom("foo").column("bar").column("baz")).hasCql("SELECT bar,baz FROM foo");
    assertThat(selectFrom("foo").selectors(getColumn("bar"), getColumn("baz")))
        .hasCql("SELECT bar,baz FROM foo");
  }

  @Test
  public void should_generate_arithmetic_selectors() {
    assertThat(selectFrom("foo").sum(getColumn("bar"), getColumn("baz")))
        .hasCql("SELECT bar+baz FROM foo");
    assertThat(selectFrom("foo").difference(raw("1"), getSum(getColumn("bar"), getColumn("baz"))))
        .hasCql("SELECT 1-(bar+baz) FROM foo");
    assertThat(selectFrom("foo").opposite(getSum(getColumn("bar"), getColumn("baz"))))
        .hasCql("SELECT -(bar+baz) FROM foo");
    assertThat(
            selectFrom("foo")
                .product(getOpposite(getColumn("bar")), getSum(getColumn("baz"), literal(1))))
        .hasCql("SELECT -bar*(baz+1) FROM foo");
    assertThat(selectFrom("foo").quotient(literal(1), getSum(getColumn("bar"), getColumn("baz"))))
        .hasCql("SELECT 1/(bar+baz) FROM foo");
    assertThat(
            selectFrom("foo").quotient(literal(1), getProduct(getColumn("bar"), getColumn("baz"))))
        .hasCql("SELECT 1/(bar*baz) FROM foo");
  }

  @Test
  public void should_generate_field_selectors() {
    assertThat(selectFrom("foo").field("user", "name")).hasCql("SELECT user.name FROM foo");
    assertThat(selectFrom("foo").field(getField("user", "address"), "city"))
        .hasCql("SELECT user.address.city FROM foo");
  }

  @Test
  public void should_generate_element_selectors() {
    assertThat(selectFrom("foo").element("m", literal(1))).hasCql("SELECT m[1] FROM foo");
    assertThat(selectFrom("foo").element(getElement("m", literal("bar")), literal(1)))
        .hasCql("SELECT m['bar'][1] FROM foo");
  }

  @Test
  public void should_generate_range_selectors() {
    assertThat(selectFrom("foo").range("s", literal(1), literal(5)))
        .hasCql("SELECT s[1..5] FROM foo");
    assertThat(selectFrom("foo").range("s", literal(1), null)).hasCql("SELECT s[1..] FROM foo");
    assertThat(selectFrom("foo").range("s", null, literal(5))).hasCql("SELECT s[..5] FROM foo");
  }

  @Test
  public void should_generate_collection_and_tuple_selectors() {
    assertThat(selectFrom("foo").listOf(getColumn("a"), getColumn("b"), getColumn("c")))
        .hasCql("SELECT [a,b,c] FROM foo");
    assertThat(selectFrom("foo").setOf(getColumn("a"), getColumn("b"), getColumn("c")))
        .hasCql("SELECT {a,b,c} FROM foo");
    assertThat(selectFrom("foo").tupleOf(getColumn("a"), getColumn("b"), getColumn("c")))
        .hasCql("SELECT (a,b,c) FROM foo");
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_fail_if_collection_selector_contains_aliases() {
    selectFrom("foo").listOf(getColumn("a"), getColumn("b").as("FORBIDDEN_HERE"), getColumn("c"));
  }

  @Test
  public void should_generate_map_selectors() {
    assertThat(
            selectFrom("foo")
                .mapOf(
                    ImmutableMap.of(
                        getColumn("k1"), getColumn("v1"), getColumn("k2"), getColumn("v2"))))
        .hasCql("SELECT {k1:v1,k2:v2} FROM foo");
    assertThat(
            selectFrom("foo")
                .mapOf(
                    ImmutableMap.of(
                        getColumn("k1"), getColumn("v1"), getColumn("k2"), getColumn("v2")),
                    DataTypes.TEXT,
                    DataTypes.INT))
        .hasCql("SELECT (map<text,int>){k1:v1,k2:v2} FROM foo");
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_fail_if_map_selector_contains_aliases() {
    selectFrom("foo")
        .mapOf(
            ImmutableMap.of(
                getColumn("k1"),
                getColumn("v1").as("FORBIDDEN_HERE"),
                getColumn("k2"),
                getColumn("v2")));
  }

  @Test
  public void should_generate_cast_selector() {
    assertThat(selectFrom("foo").cast(getColumn("k"), DataTypes.INT))
        .hasCql("SELECT (int)k FROM foo");
  }

  @Test
  public void should_generate_function_selectors() {
    assertThat(selectFrom("foo").function("f", getColumn("c1"), getSum(getColumn("c2"), raw("1"))))
        .hasCql("SELECT f(c1,c2+1) FROM foo");
    assertThat(
            selectFrom("foo")
                .function("ks", "f", getColumn("c1"), getSum(getColumn("c2"), raw("1"))))
        .hasCql("SELECT ks.f(c1,c2+1) FROM foo");
    assertThat(selectFrom("foo").writeTime("c1").ttl("c2"))
        .hasCql("SELECT writetime(c1),ttl(c2) FROM foo");
  }

  @Test
  public void should_generate_literal_selectors() {
    assertThat(selectFrom("foo").literal(1)).hasCql("SELECT 1 FROM foo");
    assertThat(selectFrom("foo").literal(Charsets.UTF_8, new CharsetCodec()))
        .hasCql("SELECT 'UTF-8' FROM foo");
  }

  @Test
  public void should_generate_raw_selector() {
    assertThat(selectFrom("foo").raw("a,b,c")).hasCql("SELECT a,b,c FROM foo");

    assertThat(selectFrom("foo").selectors(getColumn("bar"), raw("baz")))
        .hasCql("SELECT bar,baz FROM foo");
  }

  @Test
  public void should_alias_selectors() {
    assertThat(selectFrom("foo").column("bar").as("baz")).hasCql("SELECT bar AS baz FROM foo");
    assertThat(selectFrom("foo").selectors(getColumn("bar").as("c1"), getColumn("baz").as("c2")))
        .hasCql("SELECT bar AS c1,baz AS c2 FROM foo");
  }

  @Test(expected = IllegalStateException.class)
  public void should_fail_to_alias_star_selector() {
    selectFrom("foo").all().as("allthethings");
  }

  @Test(expected = IllegalStateException.class)
  public void should_fail_to_alias_if_no_selector_yet() {
    selectFrom("foo").as("bar");
  }

  @Test
  public void should_keep_last_alias_if_aliased_twice() {
    assertThat(selectFrom("foo").countAll().as("allthethings").as("total"))
        .hasCql("SELECT count(*) AS total FROM foo");
  }
}
