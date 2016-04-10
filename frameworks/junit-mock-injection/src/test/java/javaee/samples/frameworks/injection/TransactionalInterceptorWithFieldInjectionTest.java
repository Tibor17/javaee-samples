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

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import javaee.samples.frameworks.injection.transactional.injection.field.MandatoryOuterService;
import javaee.samples.frameworks.injection.transactional.injection.field.NeverOuterService;
import javaee.samples.frameworks.injection.transactional.injection.field.OuterService;
import javaee.samples.frameworks.injection.transactional.injection.field.SupportsOuterService;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.TransactionalException;

import static javaee.samples.frameworks.injection.JPARuleBuilder.unitName;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Enclosed.class)
public class TransactionalInterceptorWithFieldInjectionTest {

    @RunWith(InjectionRunner.class)
    public static class WithoutManagedTransactions {

        @Rule
        public final JPARule jpa = unitName("containerless-test-pu").build();

        @Inject
        private OuterService outerService;

        @Test
        public void shouldNotPersistDeepTransaction() {
            outerService.saveOuter("transaction");
            long storedRecords =
                    jpa.getCurrentEntityManager()
                            .createQuery("select count(e) from MyEntity e", Long.class)
                            .getSingleResult();
            assertThat(storedRecords, is(0L));
        }

    }

    @RunWith(InjectionRunner.class)
    @WithManagedTransactions
    public static class UsingManagedTransactions {

        @Rule
        public final JPARule jpa = unitName("containerless-test-pu").build();

        @Inject
        private OuterService outerService;

        @Test
        public void shouldPersistDeepTransaction() {
            outerService.saveOuter("transaction1");
            long storedRecords = jpa.getCurrentEntityManager()
                    .createQuery("select count(e) from MyEntity e", Long.class)
                            .getSingleResult();
            assertThat(storedRecords, is(1L));

            outerService.saveOuter("transaction2");
            storedRecords = jpa.getCurrentEntityManager()
                    .createQuery("select count(e) from MyEntity e", Long.class)
                            .getSingleResult();
            assertThat(storedRecords, is(2L));
        }

    }

    @RunWith(InjectionRunner.class)
    @WithManagedTransactions
    public static class SupportsTransactions {

        @Rule
        public final JPARule jpa = unitName("containerless-test-pu").build();

        @Inject
        private SupportsOuterService outerService;

        @Test
        public void shouldPersistOnExistingSupportsTransaction() {
            long storedRecords = jpa.$(() -> {
                outerService.saveOuter("transaction");
                return jpa.getCurrentEntityManager()
                        .createQuery("select count(e) from MyEntity e", Long.class)
                        .getSingleResult();
            });
            assertThat(storedRecords, is(1L));
        }

        @Test
        public void shouldNotPersistOnNonExistingSupportsTransaction() {
            outerService.saveOuter("transaction");
            long storedRecords = jpa.getCurrentEntityManager()
                    .createQuery("select count(e) from MyEntity e", Long.class)
                    .getSingleResult();
            assertThat(storedRecords, is(0L));
        }

    }

    @RunWith(InjectionRunner.class)
    @WithManagedTransactions
    public static class NeverTransactions {

        private final JPARule jpa = unitName("containerless-test-pu").build();

        private final ExpectedException exceptionHandler = ExpectedException.none();

        @Rule
        public final TestRule aop = RuleChain.outerRule(exceptionHandler).around(jpa);

        @Inject
        private NeverOuterService outerService;

        @Test
        public void shouldNotPersistOnNeverTransaction() {
            outerService.saveOuter("transaction");
            long storedRecords = jpa.getCurrentEntityManager()
                    .createQuery("select count(e) from MyEntity e", Long.class)
                    .getSingleResult();
            assertThat(storedRecords, is(0L));
        }

        @Test
        @Transactional
        public void shouldFailOnNeverTransaction() {
            exceptionHandler.expect(TransactionalException.class);
            exceptionHandler.expectMessage("Transaction not applicable to " +
                    "@Transactional(value = Transactional.TxType.NEVER) " +
                    "in bean method call " +
                    "public void javaee.samples.frameworks.injection.transactional.injection.field.NeverOuterService.saveOuter(java.lang.String)");

                outerService.saveOuter("transaction");
                jpa.getCurrentEntityManager()
                        .createQuery("select count(e) from MyEntity e", Long.class)
                        .getSingleResult();
        }

    }

    @RunWith(InjectionRunner.class)
    @WithManagedTransactions
    public static class MandatoryTransactions {

        private final JPARule jpa = unitName("containerless-test-pu").build();

        private final ExpectedException exceptionHandler = ExpectedException.none();

        @Rule
        public final TestRule aop = RuleChain.outerRule(exceptionHandler).around(jpa);

        @Inject
        private MandatoryOuterService outerService;

        @Test
        public void shouldFailOnNonExistingMandatoryTransaction() {
            exceptionHandler.expect(TransactionalException.class);
            exceptionHandler.expectMessage("Transaction not applicable to " +
                    "@Transactional(value = Transactional.TxType.MANDATORY) " +
                    "in bean method call " +
                    "public void javaee.samples.frameworks.injection.transactional.injection.field.MandatoryOuterService.saveOuter(java.lang.String)");

            outerService.saveOuter("transaction");
            jpa.getCurrentEntityManager()
                    .createQuery("select count(e) from MyEntity e", Long.class)
                    .getSingleResult();
        }

        @Test
        @Transactional
        public void shouldPersistOnExternalMandatoryTransaction() {
            outerService.saveOuter("transaction");
            long storedRecords = jpa.getCurrentEntityManager()
                    .createQuery("select count(e) from MyEntity e", Long.class)
                        .getSingleResult();
            assertThat(storedRecords, is(1L));
        }

    }

}
