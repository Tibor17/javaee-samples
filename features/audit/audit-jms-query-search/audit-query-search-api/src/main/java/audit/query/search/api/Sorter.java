/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package audit.query.search.api;

import java.io.Serializable;
import java.util.Objects;

import static java.util.Objects.*;

public class Sorter<T extends Serializable & Comparable<T>> {
    private final String fieldName;
    private final boolean ascending;
    private final Class<T> fieldType;

    public Sorter(String fieldName, boolean ascending, Class<T> fieldType) {
        this.fieldName = requireNonNull(fieldName);
        this.ascending = ascending;
        this.fieldType = requireNonNull(fieldType);
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isAscending() {
        return ascending;
    }

    public Class<T> getFieldType() {
        return fieldType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sorter<?> sorter = (Sorter<?>) o;
        return Objects.equals(getFieldType(), sorter.getFieldType())
                && Objects.equals(getFieldName(), sorter.getFieldName());
    }

    @Override
    public int hashCode() {
        return hash(getFieldType(), getFieldName());
    }
}
