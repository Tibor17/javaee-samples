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
package javaee.samples.frameworks.junitjparule.spi;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import java.lang.annotation.Annotation;
import java.util.Hashtable;
import java.util.Optional;

import static java.util.Optional.*;

public final class ResourcePoint implements InjectionPoint<Resource>, InitialContextFactoryBuilder {
    private final Context ctx;

    public ResourcePoint() {
        try {
            InitialContextFactory factory = createInitialContextFactory();
            ctx = factory.getInitialContext(new Hashtable<>());
        } catch (NamingException e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }

    public Context getNamingContext() {
        return ctx;
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return Resource.class;
    }

    @Override
    public <T> Optional<Object> lookupOf(Class<?> declaredInjectionType, Resource ann, T bean, Class<? extends T> beanType) {
        return lookupOptionally(declaredInjectionType, ann.lookup());
    }

    @Override
    public void destroy() {
        try {
            ctx.close();
        } catch (NamingException e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }

    private @SuppressWarnings("unchecked") <R>
    Optional<R> lookupOptionally(Class<?> declaredInjectionType, String lookupName) {
        try {
            Object o = ctx.lookup(lookupName);
            return o != null && declaredInjectionType.isAssignableFrom(o.getClass()) ? of((R) o) : empty();
        } catch (NamingException e) {
            return empty();
        }
    }

    @Override
    public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException {
        return new Factory(environment);
    }

    final class Factory implements InitialContextFactory {
        private final Hashtable<?, ?> environment;

        Factory(Hashtable<?, ?> environment) {
            this.environment = environment;
        }

        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
            return new InitialContext(this.environment);
        }
    }

    public InitialContextFactory createInitialContextFactory() {
        try {
            return createInitialContextFactory(new Hashtable<>());
        } catch (NamingException e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }
}
