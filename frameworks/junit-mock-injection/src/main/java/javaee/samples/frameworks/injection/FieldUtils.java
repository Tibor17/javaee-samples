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
package javaee.samples.frameworks.injection;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import static javaee.samples.frameworks.injection.NullFilter.NULL_FILTER;

/**
 * Hint for classes http://stackoverflow.com/questions/3403909/get-generic-type-of-class-at-runtime .
 */
final class FieldUtils {
    private FieldUtils() {
        throw new IllegalStateException("not instantiable constructor");
    }

    static Collection<Class<?>> filterGenericTypes(Field f) {
        return filterGenericTypes(f, NULL_FILTER);
    }

    static Collection<Class<?>> filterGenericTypes(Field f, Filter<Class<?>> filter) {
        Collection<Class<?>> gTypes = new ArrayList<>();
        Type generic = f.getGenericType();
        if (generic instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) generic;
            for (Type gType : pType.getActualTypeArguments()) {
                if (gType instanceof Class && filter.matches((Class<?>) gType)) {
                    gTypes.add((Class<?>) gType);
                }
            }
        }
        return gTypes;
    }
}
