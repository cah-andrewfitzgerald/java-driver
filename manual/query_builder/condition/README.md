## Conditions

A condition is a clause that appears after the IF keyword in a conditional [UPDATE](../update/) or
[DELETE](../delete/) statement.

To create a condition, call one of the `ifXxx()` methods of [QueryBuilderDsl], chain it with one of
the available "operator" methods, and pass the result to `if_()`:

```java
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.*;

Condition vCondition = ifColumn("v").eq(literal(1));
deleteFrom("user")
    .whereColumn("k").eq(bindMarker())
    .if_(vCondition);
// DELETE FROM user WHERE k=? IF v=1
```

If you call `if_()` multiple times, the clauses will be joined with the AND keyword. You can also
add multiple conditions in a single call. This is a bit more efficient since it creates less
temporary objects:

```java
deleteFrom("user")
    .whereColumn("k").eq(bindMarker())
    .if_(
        ifColumn("v1").eq(literal(1)), 
        ifColumn("v2").eq(literal(2)));
// DELETE FROM user WHERE k=? IF v1=1 AND v2=2
```

Finally, there are fluent shortcuts to create and add the condition in a single call. This is
probably the most readable if you're building the query statically:

```java
deleteFrom("user")
    .whereColumn("k").eq(bindMarker())
    .ifColumn("v1").eq(literal(1))
    .ifColumn("v2").eq(literal(2));
// DELETE FROM user WHERE k=? IF v1=1 AND v2=2    
```

Conditions are composed of a left operand, an operator, and a right-hand-side
[term](../term/).

### Simple columns

`ifColumn` operates on a single column. It supports basic arithmetic comparison operators:

| Comparison operator | Method name |
|---------------------|-------------|
| `=`                 | `eq`        |
| `<`                 | `lt`        |
| `<=`                | `lte`       |
| `>`                 | `gt`        |
| `>=`                | `gte`       |
| `!=`                | `ne`        |

*Note: we support `!=` because it is present in the CQL grammar but, as of Cassandra 4, it is not
implemented yet.*

In addition, `in()` can test for equality with various alternatives. You can either provide each
alternative as a term:

```java
deleteFrom("user")
    .whereColumn("k").eq(bindMarker())
    .ifColumn("v").in(bindMarker(), bindMarker(), bindMarker());
// DELETE FROM user WHERE k=? IF v IN (?,?,?)
```

Or bind the whole list of alternatives as a single variable:

```java
deleteFrom("user")
    .whereColumn("k").eq(bindMarker())
    .ifColumn("v").in(bindMarker());
// DELETE FROM user WHERE k=? IF v IN ?
```

### UDT fields

`ifField` tests a field in a top-level UDT (nested UDTs are not allowed):

```java
deleteFrom("user")
    .whereColumn("k").eq(bindMarker())
    .ifField("address", "zip").eq(literal(94040));
// DELETE FROM user WHERE k=? IF address.zip=94040
```

It supports the same set of operators as simple columns.

### Collection elements

`ifElement` tests an element in a top-level collection (nested collections are not allowed):

```java
deleteFrom("product")
    .whereColumn("sku").eq(bindMarker())
    .ifElement("features", literal("color")).in(literal("red"), literal("blue"));
// DELETE FROM product WHERE sku=? IF features['color'] IN ('red','blue')
```

It supports the same set of operators as simple columns.

### Raw snippets

You can also provide a condition as a raw CQL snippet, that will get appended to the query as-is,
without any syntax checking or escaping:

```java
deleteFrom("product")
    .whereColumn("sku").eq(bindMarker())
    .ifRaw("features['color'] IN ('red', 'blue') /*some random comment*/");
// DELETE FROM product WHERE sku=? IF features['color'] IN ('red', 'blue') /*some random comment*/
```

This should be used with caution, as it's possible to generate invalid CQL that will fail at
execution time; on the other hand, it can be used as a workaround to handle new CQL features that
are not yet covered by the query builder.

### IF EXISTS

Finally, you can specify an IF EXISTS clause:

```java
deleteFrom("product").whereColumn("sku").eq(bindMarker()).ifExists();
// DELETE FROM product WHERE sku=? IF EXISTS
```

It is mutually exclusive with column conditions: if you previously specified column conditions on
the statement, they will be ignored; conversely, adding a column condition cancels a previous IF
EXISTS clause.

[QueryBuilderDsl]: http://docs.datastax.com/en/drivers/java/4.0/com/datastax/oss/driver/api/query-builder/QueryBuilderDsl.html
