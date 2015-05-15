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

import javaee.samples.frameworks.junitjparule.JPARule;
import org.junit.Rule;
import org.junit.Test;

import static javaee.samples.frameworks.junitjparule.JPARuleBuilder.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public class DummyTest {
    @Rule
    public final JPARule rule = unitName("containerless-test-pu").build();

    @Test
    public void shouldReloadStoredEntityWithUserTransaction() {
        rule.currentEntityManager().getTransaction().begin();
        A expected = new A();
        expected.s = "my string";
        rule.currentEntityManager().persist(expected);
        assertNotNull(expected.id);
        assertThat(expected.id, is(greaterThan(0L)));
        rule.currentEntityManager().getTransaction().commit();
        rule.currentEntityManager().close();
        A actual = rule.currentEntityManager().find(A.class, expected.id);
        assertNotNull(actual);
        assertNotSame(expected, actual);
        assertThat(actual.id, is(greaterThan(0L)));
        assertThat(actual.id, is(expected.id));
        assertThat(actual.s, is(expected.s));
    }

    @Test
    public void shouldReloadStoredEntityWithManagedTransaction() {
        A expected = rule.$(em -> {
            A a = new A();
            a.s = "my string";
            em.persist(a);
            return a;
        });
        assertNotNull(expected.id);
        assertThat(expected.id, is(greaterThan(0L)));
        A actual = rule.currentEntityManager().find(A.class, expected.id);
        assertNotNull(actual);
        assertNotSame(expected, actual);
        assertThat(actual.id, is(greaterThan(0L)));
        assertThat(actual.id, is(expected.id));
        assertThat(actual.s, is(expected.s));
    }
}
