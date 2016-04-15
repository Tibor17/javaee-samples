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

import java.util.Objects;

import static java.util.Objects.*;

public abstract class Matcher<T> {
    private final Object searchedText;

    public Matcher(Object searchedText) {
        this.searchedText = requireNonNull(searchedText);
    }

    public abstract String getFieldName();
    public abstract Class<T> getEntityType();

    public Object getSearchedText() {
        return searchedText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Matcher<?> matcher = (Matcher<?>) o;
        return Objects.equals(getFieldName(), matcher.getFieldName())
                && Objects.equals(getEntityType(), matcher.getEntityType());
    }

    @Override
    public int hashCode() {
        return hash(getFieldName(), getEntityType());
    }

    public static <T> Matcher<T> field(Object searchedText, String fieldName, Class<T> entityType) {
        return new StringFieldMatcher<>(searchedText, fieldName, entityType);
    }
}
