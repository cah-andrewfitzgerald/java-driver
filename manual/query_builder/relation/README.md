## Relations

A relation is a clause that appears after the WHERE keyword, and restricts the rows that the
statement operates on.

Relations are used by the following statements:

* [SELECT](../select/) 
* [UPDATE](../update/)
* [DELETE](../delete/)

To create a relation, call one of the `isXxx()` methods of `QueryBuilderDsl`, chain it with one of
the available "operator" methods, and pass the result to `where()`:

```java
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.*;

Relation idRelation = isColumn("id").eq(bindMarker());
selectFrom("user").all().where(idRelation);
// SELECT * FROM user WHERE id=?
```

If you call `where()` multiple times, the clauses will be joined with the AND keyword. You can also
add multiple relations in a single call. This is a bit more efficient since it creates less
temporary objects: 

```java
selectFrom("sensor_data").all()
    .where(
        isColumn("id").eq(bindMarker()),
        isColumn("date").gt(bindMarker()));
// SELECT * FROM sensor_data WHERE id=? AND date>?
```

Finally, there are fluent shortcuts to create and add the selector in a single call. This is
probably the most readable if you're building the query statically:

```java
selectFrom("sensor_data").all()
    .whereColumn("id").eq(bindMarker())
    .whereColumn("date").gt(bindMarker());
// SELECT * FROM sensor_data WHERE id=? AND date>?
```

Relations are generally composed of a left-hand-side target, an operator, and an optional
right-hand-side [term](../term/). The type of relation determines which operators are available.
 
### Simple columns

`isColumn` operates on a single column. It supports basic arithmetic comparison operators:

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

See above for comparison operator examples.

If you're using SASI indices, you can also use `like()` for wildcard comparisons:

```java
selectFrom("user").all().where(isColumn("last_name").like(literal("M%")));
// SELECT * FROM user WHERE last_name LIKE 'M%'
```

`in()` is like `eq()`, but with various alternatives. You can either provide each alternative as a
term:

```java
selectFrom("user").all().where(isColumn("id").in(literal(1), literal(2), literal(3)));
// SELECT * FROM user WHERE id IN (1,2,3)

selectFrom("user").all().where(isColumn("id").in(bindMarker(), bindMarker(), bindMarker()));
// SELECT * FROM user WHERE id IN (?,?,?)
```

Or bind the whole list of alternatives as a single variable:

```java
selectFrom("user").all().where(isColumn("id").in(bindMarker()));
// SELECT * FROM user WHERE id IN ?
```

For collection columns, you can check for the presence of an element with `contains()` and
`containsKey()`:

```java
selectFrom("sensor_data")
    .all()
    .where(
        isColumn("id").eq(bindMarker()),
        isColumn("date").eq(bindMarker()),
        isColumn("readings").containsKey(literal("temperature")))
    .allowFiltering();
// SELECT * FROM sensor_data WHERE id=? AND date=? AND readings CONTAINS KEY 'temperature' ALLOW FILTERING
```

Finally, `notNull()` generates an `IS NOT NULL` check. *Note: we support `IS NOT NULL` because it is
present in the CQL grammar but, as of Cassandra 4, it is not implemented yet.*

### Column components

`isColumnComponent` operates on an element inside of a complex column (as of Cassandra 4, this only
works with map values, but the concept could easily be extended to list elements).

```java
selectFrom("sensor_data")
    .all()
    .where(
        isColumn("id").eq(bindMarker()),
        isColumn("date").eq(bindMarker()),
        isColumnComponent("readings", literal("temperature")).gt(literal(65)))
    .allowFiltering();
// SELECT * FROM sensor_data WHERE id=? AND date=? AND readings['temperature']>65 ALLOW FILTERING
```

Column components support the six basic arithmetic comparison operators.

### Tokens

`isToken` hashes one or more columns into a token. It is generally used to perform range queries:

```java
selectFrom("user")
    .all()
    .where(
        isToken("id").gt(bindMarker()),
        isToken("id").lte(bindMarker()));
// SELECT * FROM user WHERE token(id)>? AND token(id)<=?
```

It supports the six basic arithmetic comparison operators.

### Tuples

`isTuple` compares a tuple of columns to tuple terms of the same arity. It supports the six basic
arithmetic comparison operators (using lexicographical order):

```java
selectFrom("sensor_data")
    .all()
    .where(
        isToken("id").eq(bindMarker()),
        isTuple("date", "hour").gt(tuple(bindMarker(), bindMarker())));
// SELECT * FROM sensor_data WHERE token(id)=? AND (date,hour)>(?,?)
```

In addition, tuples support the `in()` operator. Like with regular columns, bind markers can operate
at different levels:

```java
// Bind the whole list of alternatives (two-element tuples) as a single value:
selectFrom("test")
    .all()
    .where(
        isColumn("k").eq(literal(1)),
        isTuple("c1", "c2").in(bindMarker()));
// SELECT * FROM test WHERE k=1 AND (c1,c2) IN ?

// Bind each alternative as a value:
selectFrom("test")
    .all()
    .where(
        isColumn("k").eq(literal(1)),
        isTuple("c1", "c2").in(bindMarker(), bindMarker(), bindMarker()));
// SELECT * FROM test WHERE k=1 AND (c1,c2) IN (?,?,?)

// Bind each element in the alternatives as a value:
selectFrom("test")
    .all()
    .where(
        isColumn("k").eq(literal(1)),
        isTuple("c1", "c2")
            .in(
                tuple(bindMarker(), bindMarker()),
                tuple(bindMarker(), bindMarker()),
                tuple(bindMarker(), bindMarker())));
// SELECT * FROM test WHERE k=1 AND (c1,c2) IN ((?,?),(?,?),(?,?))
```

### Custom index expressions

`isCustomIndex` evaluates a custom index. The argument is a free-form term (what is a legal value
depends on your index implementation):

```java
selectFrom("foo")
    .all()
    .where(isColumn("k").eq(literal(1)))
    .where(isCustomIndex("my_custom_index", literal("a text expression")));
// SELECT * FROM foo WHERE k=1 AND expr(my_custom_index,'a text expression')
```

### Raw snippets

Finally, it is possible to provide a raw CQL snippet with `raw()`; it will get appended to the query
as-is, without any syntax checking or escaping:

```java
selectFrom("foo").all().where(raw("k = 1 /*some custom comment*/ AND c<2"));
// SELECT * FROM foo WHERE k = 1 /*some custom comment*/ AND c<2
```

This should be used with caution, as it's possible to generate invalid CQL that will fail at
execution time; on the other hand, it can be used as a workaround to handle new CQL features that
are not yet covered by the query builder.