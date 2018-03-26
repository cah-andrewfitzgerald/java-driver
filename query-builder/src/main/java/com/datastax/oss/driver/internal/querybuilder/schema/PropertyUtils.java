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
package com.datastax.oss.driver.internal.querybuilder.schema;

import java.util.Map;

public class PropertyUtils {

  public static String buildProperties(Map<String, Object> properties, boolean first) {
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<String, Object> property : properties.entrySet()) {
      if (first) {
        builder.append(" WITH ");
        first = false;
      } else {
        builder.append(" AND ");
      }
      String value = PropertyUtils.extractPropertyValue(property.getValue());
      builder.append(property.getKey()).append("=").append(value);
    }
    return builder.toString();
  }

  private static String extractPropertyValue(Object property) {
    StringBuilder propertyValue = new StringBuilder();
    if (property instanceof String) {
      propertyValue.append("'").append((String) property).append("'");
    } else if (property instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> propertyMap = (Map<String, Object>) property;
      boolean first = true;
      propertyValue.append("{");
      for (Map.Entry<String, Object> subProperty : propertyMap.entrySet()) {
        if (first) {
          first = false;
        } else {
          propertyValue.append(",");
        }
        propertyValue
            .append("'")
            .append(subProperty.getKey())
            .append("':")
            .append(extractPropertyValue(subProperty.getValue()));
      }
      propertyValue.append("}");
    } else {
      propertyValue.append(property);
    }
    return propertyValue.toString();
  }
}
