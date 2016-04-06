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

import org.junit.Test;
import org.junit.runner.Description;

import javax.enterprise.util.AnnotationLiteral;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;

import static javaee.samples.frameworks.junitjparule.JPARule.canRollback;
import static org.junit.runner.Description.createTestDescription;
import static org.assertj.core.api.Assertions.*;

public class JPARuleTest {
    static class T extends AnnotationLiteral<Transactional> implements Transactional {
        private final TxType type;
        private final Class rollbackOn, dontRollbackOn;

        T(TxType type, Class rollbackOn, Class dontRollbackOn) {
            this.type = type;
            this.rollbackOn = rollbackOn;
            this.dontRollbackOn = dontRollbackOn;
        }

        @Override
        public TxType value() {
            return type;
        }

        @Override
        public Class[] rollbackOn() {
            return rollbackOn == null ? new Class[0] : new Class[]{rollbackOn};
        }

        @Override
        public Class[] dontRollbackOn() {
            return dontRollbackOn == null ? new Class[0] : new Class[]{dontRollbackOn};
        }
    }

    @Test
    public void cannotRollbackWithoutAnnotation() {
        Description description = createTestDescription(getClass(), "");
        boolean rollback = canRollback(new PersistenceException(), description);
        assertThat(rollback).isTrue();
    }

    @Test
    public void canRollbackWithAnnotation() {
        Description description = createTestDescription(getClass(), "", new T(null, null, null));
        boolean rollback = canRollback(new PersistenceException(), description);
        assertThat(rollback).isTrue();
    }

    @Test
    public void canRollbackWithSubAnnotation() {
        Description description = createTestDescription(getClass(), "", new T(null, PersistenceException.class, null));
        boolean rollback = canRollback(new OptimisticLockException(), description);
        assertThat(rollback).isTrue();
    }

    static class MailException extends RuntimeException {}
    static class IllegalMailTemplateException extends MailException {}

    @Test
    public void cannotRollbackWithSubAnnotation() {
        Description description = createTestDescription(getClass(), "", new T(null, null, MailException.class));
        boolean rollback = canRollback(new IllegalMailTemplateException(), description);
        assertThat(rollback).isFalse();
    }

    @Test
    public void cannotRollbackWithBothAssignable() {
        Description description = createTestDescription(getClass(), "", new T(null, RuntimeException.class, MailException.class));
        boolean rollback = canRollback(new IllegalMailTemplateException(), description);
        assertThat(rollback).isFalse();
    }
}
