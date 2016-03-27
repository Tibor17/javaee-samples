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

import org.junit.BeforeClass;
import org.junit.Test;

import javax.enterprise.event.Event;
import java.util.Collection;
import java.util.Map;

import static javaee.samples.frameworks.junitjparule.FieldUtils.filterGenericTypes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

public class FieldUtilsTest {
    private static Filter<Class<?>> filter;

    @SuppressWarnings("unused")
    private Event<String> event;

    @SuppressWarnings("unused")
    private Map<Integer, String> mapReduce;

    @BeforeClass
    public static void init() {
        filter = arg -> arg == String.class;
    }

    @Test
    public void shouldFindSingleGenericParameter() throws Exception {
        Collection<Class<?>> c = filterGenericTypes(getClass().getDeclaredField("event"), filter);
        assertThat(c, hasSize(1));
        assertThat(c, hasItem(String.class));
    }

    @Test
    public void shouldFindSecondGenericParameter() throws Exception {
        Collection<Class<?>> c = filterGenericTypes(getClass().getDeclaredField("mapReduce"), filter);
        assertThat(c, hasSize(1));
        assertThat(c, hasItem(String.class));
    }

    @Test
    public void shouldFindAllGenericParameters() throws Exception {
        Collection<Class<?>> c = filterGenericTypes(getClass().getDeclaredField("mapReduce"));
        assertThat(c, hasSize(2));
        assertThat(c, hasItem(Integer.class));
        assertThat(c, hasItem(String.class));
    }
}
