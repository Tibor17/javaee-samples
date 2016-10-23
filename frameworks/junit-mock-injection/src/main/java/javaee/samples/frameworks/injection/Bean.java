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

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

final class Bean<T> {
    private final Class<T> beanClass;
    private final T delegate;
    private final T proxy;

    Bean(Class<T> beanClass, T delegate, T proxy) {
        this.beanClass = beanClass;
        this.delegate = delegate;
        this.proxy = proxy;
    }

    @SuppressWarnings("unused")
    Bean(Class<T> beanClass, T realObject) {
        this(beanClass, realObject, realObject);
        if (realObject != null && beanClass != realObject.getClass()) {
            throw new IllegalArgumentException();
        }
    }

    @SuppressWarnings("unused")
    Class<T> getBeanClass() {
        return beanClass;
    }

    boolean hasDelegate() {
        return nonNull(delegate);
    }

    T getDelegate() {
        return requireNonNull(delegate, "null delegate must not be retrieved");
    }

    boolean hasProxy() {
        return nonNull(proxy);
    }

    T getProxy() {
        return proxy;
    }

    @Override
    public String toString() {
        return "Bean{" + "beanClass=" + beanClass.getSimpleName() + ", delegate=" + delegate + ", proxy=" + proxy + '}';
    }
}
