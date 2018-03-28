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
package com.datastax.oss.driver.api.querybuilder.schema;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.querybuilder.BuildableQuery;

public interface CreateTableWithColumns
    extends BuildableQuery, OngoingCreateTable, CreateTableWithOptions {

  CreateTableWithColumns withClusteringColumn(CqlIdentifier columnName, DataType dataType);

  default CreateTableWithColumns withClusteringColumn(String columnName, DataType dataType) {
    return withClusteringColumn(CqlIdentifier.fromCql(columnName), dataType);
  }

  CreateTableWithColumns withColumn(CqlIdentifier columnName, DataType dataType);

  default CreateTableWithColumns withColumn(String columnName, DataType dataType) {
    return withColumn(CqlIdentifier.fromCql(columnName), dataType);
  }

  CreateTableWithColumns withStaticColumn(CqlIdentifier columnName, DataType dataType);

  default CreateTableWithColumns withStaticColumn(String columnName, DataType dataType) {
    return withStaticColumn(CqlIdentifier.fromCql(columnName), dataType);
  }
}
