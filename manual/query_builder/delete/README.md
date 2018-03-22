## DELETE

To start a DELETE query, use one of the `deleteFrom` method in [QueryBuilderDsl]. There are several
variants depending on whether your table name is qualified, and whether you use case-sensitive
identifiers or case-insensitive strings:

```java
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.*;

DeleteSelection delete = deleteFrom("user");
```

Note that, at this stage, the query can't be built yet. You need at least one
[relation](#relations).

### Selectors

A selector is something that appears after the `DELETE` keyword, and will be removed from the
affected row(s).

Selectors are optional; if you don't provide any, the whole row will be deleted.

To create a selector, use one of the `getXxx()` methods of `QueryBuilderDsl`, and pass it to the
`selector()` method:

```java
deleteFrom("user").selector(getColumn("v"))
// DELETE v FROM user ...
```

If you have multiple selectors, you can also use `selectors()` to add them all in a single call.
This is a bit more efficient since it creates less temporary objects: 

```java
deleteFrom("user").selectors(getColumn("v1"), getColumn("v2"));
// DELETE v1,v2 FROM user...
```

Finally, there are fluent shortcuts to create and add the selector in a single call. This is
probably the most readable if you're building the query statically:

```java
deleteFrom("user").column("v1").column("v2");
// DELETE v1,v2 FROM user...
```

Only 3 types of selectors can be used in DELETE statements:

* simple columns (as illustrated in the previous examples);
* fields in non-nested UDT columns:

  ```java
  deleteFrom("user").field("address", "street");
  // DELETE address.street FROM user ...
  ```
  
* elements in non-nested collection columns:

  ```java
  deleteFrom("product").element("features", literal("color"));
  // DELETE features['color'] FROM product ...
  ```
  
You can also pass a raw CQL snippet, that will get appended to the query as-is, without any syntax
checking or escaping:

```java
deleteFrom("user").raw("v /*some random comment*/")
// DELETE v /*some random comment*/ FROM user ...
```

This should be used with caution, as it's possible to generate invalid CQL that will fail at
execution time; on the other hand, it can be used as a workaround to handle new CQL features that
are not yet covered by the query builder.

### Timestamp

The USING TIMESTAMP clause specifies the timestamp at which the mutation will be applied. You can
pass either a literal value:

```java
deleteFrom("user").column("v").usingTimestamp(1234)
// DELETE v FROM user USING TIMESTAMP 1234
```

Or a bind marker:

```java
deleteFrom("user").column("v").usingTimestamp(bindMarker())
// DELETE v FROM user USING TIMESTAMP ?
```

If you call the method multiple times, the last value will be used.

### Relations

Relations get added with the `where()` method:

```java
deleteFrom("user").where(isColumn("k").eq(bindMarker()));
// DELETE FROM user WHERE k=?
```

Like selectors, they also have fluent shortcuts to build and add in a single call:

```java
deleteFrom("user").whereColumn("k").eq(bindMarker());
// DELETE FROM user WHERE k=?
```

Once there is at least one relation, the statement can be built:

```java
SimpleStatement statement = deleteFrom("user").whereColumn("k").eq(bindMarker()).build();
```

Relations are a common feature used by many types of statements, so they have a
[dedicated page](../relation) in this manual.

### Conditions

Conditions get added with the `if_()` method:

```java
deleteFrom("user").whereColumn("k").eq(bindMarker()).if_(ifColumn("v").eq(literal(1)));
// DELETE FROM user WHERE k=? IF v=1
```

Like selectors and relations, they also have fluent shortcuts to build and add in a single call:

```java
deleteFrom("user").whereColumn("k").eq(bindMarker()).ifColumn("v").eq(literal(1));
// DELETE FROM user WHERE k=? IF v=1
```

Conditions are a common feature used by UPDATE and DELETE, so they have a
[dedicated page](../condition) in this manual.

[QueryBuilderDsl]: http://docs.datastax.com/en/drivers/java/4.0/com/datastax/oss/driver/api/query-builder/QueryBuilderDsl.html
