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

import javax.persistence.Entity;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;

import static javaee.samples.frameworks.injection.FieldUtils.filterGenericTypes;

final class DomainUtils {

    private DomainUtils() {
        throw new IllegalStateException("not instantiable constructor");
    }

    public static void printPersistenceXmlEntities(PrintStream stream, Iterable<Class<?>> examine) {
        SortedSet<String> result = new TreeSet<>();

        for (Class<?> domain : examine) {
            if (isDomainClass(domain) && result.add(domain.getName())) {
                populatePersistenceDomain(result, domain);
            }
        }

        for (String domain : result) {
            stream.printf("<class>%s</class>\n", domain);
        }
    }

    @SuppressWarnings("checkstyle:innerassignment")
    private static void populatePersistenceDomain(Set<String> result, Class<?> c) {
        do {
            for (Field f : c.getDeclaredFields()) {
                if (f != null && hasAnnotationPackage(f, "javax.persistence.")) {

                    final Class<?> type = f.getType();

                    if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {

                        filterGenericTypes(f, createDomainClassFilter())
                                .stream()
                                .filter(domain -> result.add(domain.getName()))
                                .forEach(domain -> populatePersistenceDomain(result, domain));

                    } else if (!isJavaType(type) && isDomainClass(type) && result.add(type.getName())) {
                        populatePersistenceDomain(result, type);
                    }
                }
            }
        } while ((c = c.getSuperclass()) != null);
    }

    private static boolean isLegal(Class<?> type) {
        return !type.isPrimitive() && !type.isArray() && !type.isInterface() && !type.isEnum() && !type.isSynthetic();
    }

    private static boolean isDomainClass(Class<?> type) {
        return isLegal(type) && type.isAnnotationPresent(Entity.class);
    }

    private static Filter<Class<?>> createDomainClassFilter() {
        return gType -> isDomainClass((Class<?>) gType);
    }

    private static boolean hasAnnotationPackage(AnnotatedElement f, String pkg) {
        for (Annotation a : f.getDeclaredAnnotations()) {
            if (a.annotationType().getCanonicalName().startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isJavaType(Class<?> type) {
        if (isLegal(type)) {
            String pkg = type.getPackage().getName();
            return pkg.startsWith("java.") || pkg.startsWith("javax.");
        } else {
            return false;
        }
    }
}
