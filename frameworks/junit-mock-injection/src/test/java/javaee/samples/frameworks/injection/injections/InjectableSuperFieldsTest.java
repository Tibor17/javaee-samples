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
package javaee.samples.frameworks.injection.injections;

import javaee.samples.frameworks.injection.InjectionRunner;
import javaee.samples.frameworks.injection.injections.internal.InheritanceBean;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(InjectionRunner.class)
public class InjectableSuperFieldsTest {
    @Inject
    private InheritanceBean inherited;

    @Inject
    private InjectableRunnerTest.B b;

    @Produces
    @SuppressWarnings("unused")
    private final InjectableRunnerTest.Int i = new InjectableRunnerTest.Int() {};

    @Test
    public void shouldHaveInjectedSuperFields() {
        assertThat(b, notNullValue());
        assertThat(inherited.getA(), notNullValue());
        assertThat(inherited.getB(), notNullValue());
        assertThat(b, is(sameInstance(inherited.getB())));
        assertThat(b.getA(), is(sameInstance(inherited.getA())));
    }
}
