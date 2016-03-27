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
package javaee.samples.frameworks.junitjparule.injections.gfields;

import javaee.samples.frameworks.junitjparule.InjectionRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import java.lang.annotation.Annotation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

@RunWith(InjectionRunner.class)
public class GenericInjectionTypesTest {
    @Produces
    private final Event<String> stringBasedEvent = createEvent(String.class);

    @Produces
    private Event<Integer> numberBasedEvent = createEvent(Integer.class);

    @Inject
    private GenericInjectionBean1 bean1;

    @Inject
    private GenericInjectionBean2 bean2;

    @Test
    public void shouldBeDifferentBeans() {
        assertThat(bean1.stringBasedEvent, is(sameInstance(stringBasedEvent)));
        assertThat(bean2.numberBasedEvent, is(sameInstance(numberBasedEvent)));
    }

    private <T> Event<T> createEvent(Class<T> clazz) {
        return new Event<T>() {
            @Override
            public void fire(T event) {}

            @Override
            public Event<T> select(Annotation... qualifiers) {
                return null;
            }

            @Override
            public <U extends T> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
                return null;
            }

            @Override
            public <U extends T> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
                return null;
            }
        };
    }
}
