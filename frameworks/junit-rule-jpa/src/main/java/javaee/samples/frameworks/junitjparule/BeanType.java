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
package javaee.samples.frameworks.junitjparule;

import javax.inject.Named;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

import static javaee.samples.frameworks.junitjparule.FieldUtils.filterGenericTypes;
import static java.util.Collections.unmodifiableCollection;

final class BeanType {
    private final Class<?> type;
    private final Collection<Class<?>> genericTypes;
    private final String textQualifier;
    private final Annotation[] annotationQualifiers;

    BeanType(Class<?> type, Collection<Class<?>> genericTypes, String textQualifier, Annotation... annotationQualifiers) {
        this.type = type;
        this.genericTypes = unmodifiableCollection(genericTypes);
        this.textQualifier = textQualifier;
        this.annotationQualifiers = annotationQualifiers.clone();
    }

    BeanType(Class<?> type, Collection<Class<?>> genericTypes, String textQualifier, Collection<Annotation> annotationQualifiers) {
        this(type, genericTypes, textQualifier, annotationQualifiers.toArray(new Annotation[annotationQualifiers.size()]));
    }

    BeanType(Class<?> type, Collection<Class<?>> genericTypes) {
        this(type, genericTypes, null);
    }

    BeanType(Class<?> type) {
        this(type, Collections.<Class<?>>emptySet());
    }

    @SuppressWarnings("unused")
    static BeanType createBeanType(Field f) {
        Collection<Class<?>> genericTypes = filterGenericTypes(f);
        Named stringQualifier = f.getAnnotation(Named.class);
        Collection<Annotation> qualifierAnnotations = new ArrayList<>();
        if (stringQualifier == null) {
            for (Annotation annotation : f.getAnnotations()) {
                if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                    qualifierAnnotations.add(annotation);
                }
            }
        }
        return new BeanType(f.getType(), genericTypes, stringQualifier == null ? null : stringQualifier.value(), qualifierAnnotations);
    }

    Class<?> getType() {
        return type;
    }

    @SuppressWarnings("unused")
    Collection<Class<?>> getGenericTypes() {
        return genericTypes;
    }

    @SuppressWarnings("unused")
    String getTextQualifier() {
        return textQualifier;
    }

    @SuppressWarnings("unused")
    Annotation[] getAnnotationQualifiers() {
        return annotationQualifiers.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeanType beanType = (BeanType) o;
        return Objects.equals(type, beanType.type) &&
                genericTypes.size() == beanType.genericTypes.size() &&
                genericTypes.containsAll(beanType.genericTypes) &&
                Objects.equals(textQualifier, beanType.textQualifier) &&
                Arrays.equals(annotationQualifiers, beanType.annotationQualifiers);
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + (type == null ? 0 : type.hashCode());
        result = 31 * result + genericTypes.size();
        result = 31 * result + (textQualifier == null ? 0 : textQualifier.hashCode());
        result = 31 * result + (annotationQualifiers == null ? 0 : Arrays.hashCode(annotationQualifiers));

        return result;
    }

    @Override
    public String toString() {
        return type.getName();
    }
}
