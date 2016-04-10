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

import javax.enterprise.inject.Stereotype;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class BeanUtils {

    private BeanUtils() {
        throw new IllegalStateException("not instantiable constructor");
    }

    static <T extends Annotation> T getAnnotation(Class<?> type, Class<T> annotationType) {
        Class<?> discoveredType = type;
        do {
            // Stereotypes are transitive
            // http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html
            // Chapter 2.8.1.5
            T annotation = hasAnnotationInStereotypes(discoveredType.getDeclaredAnnotations(), annotationType);
            if (annotation != null) {
                return annotation;
            }
        } while ((discoveredType = discoveredType.getSuperclass()) != null);
        return null;
    }

    @SuppressWarnings("unused")
    static boolean hasAnnotation(Class<?> type, Class<? extends Annotation> annotationType) {
        return getAnnotation(type, annotationType) != null;
    }

    @SuppressWarnings("unused")
    static boolean hasAnnotation(Field field, Class<? extends Annotation> annotation) {
        return hasAnnotationInStereotypes(field.getDeclaredAnnotations(), annotation) != null;
    }

    @SuppressWarnings("unused")
    static boolean hasAnnotation(Method method, Class<? extends Annotation> annotation) {
        return hasAnnotationInStereotypes(method.getDeclaredAnnotations(), annotation) != null;
    }

    static <T extends Annotation> T getAnnotation(Method method, Class<T> annotation) {
        return hasAnnotationInStereotypes(method.getDeclaredAnnotations(), annotation);
    }

    static <T extends Annotation> T getAnnotationDeep(Class<?> type, Class<T> annotationType) {
        T annotation = getAnnotation(type, annotationType);
        if (annotation == null) {
            for (Method method : type.getMethods()) {
                annotation = getAnnotation(method, annotationType);
                if (annotation != null) {
                    break;
                }
            }
        }
        return annotation;
    }

    static boolean hasAnnotationDeep(Class<?> type, Class<? extends Annotation> annotation) {
        return getAnnotationDeep(type, annotation) != null;
    }

    private static <T extends Annotation> T hasAnnotationInStereotypes(Annotation[] declaredAnnotations, Class<T> expectedAnnotationType) {
        for (Annotation declaredAnnotation : declaredAnnotations) {
            Class<? extends Annotation> declaredAnnotationType = declaredAnnotation.annotationType();
            if (declaredAnnotationType == expectedAnnotationType
                    || declaredAnnotationType.isAnnotationPresent(Stereotype.class)
                    && hasAnnotationInStereotypes(declaredAnnotationType.getAnnotations(), expectedAnnotationType) != null) {
                return expectedAnnotationType.cast(declaredAnnotation);
            }
        }
        return null;
    }
}
