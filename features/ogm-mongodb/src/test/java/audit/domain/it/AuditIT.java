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
package audit.domain.it;

import audit.domain.Audit;
import audit.domain.AuditChange;
import audit.domain.AuditFlow;
import audit.domain.AuditHeader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static java.util.UUID.randomUUID;

public class AuditIT {
    private static EntityManagerFactory emf;

    @BeforeClass
    public static void setUpEntityManagerFactory() {
        emf = Persistence.createEntityManagerFactory("audit-jpa");
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        emf.close();
    }

    @Test
    public void canPersistAndLoad() {
        Audit expected = $(em -> {
            AuditHeader header = new AuditHeader();
            header.setKey("hk");
            header.setValue("hv");
            em.persist(header);

            AuditChange change = new AuditChange();
            change.setKey("k");
            change.setOldValue("o");
            change.setNewValue("n");
            em.persist(change);

            AuditFlow flow = new AuditFlow();
            flow.setError("some error");
            flow.getHeaders().add(header);
            flow.getChanges().add(change);
            em.persist(flow);

            Audit a = new Audit();
            a.setRequest(randomUUID());
            a.setInitiator(1);
            a.setModule("audit-module");
            a.getFlows().add(flow);
            em.persist(a);

            return a;
        });

        Audit actual = $$(em -> em.find(Audit.class, expected.getId()));

        assertThat(actual)
                .isNotSameAs(expected);

        assertThat(actual.getRequest())
                .isEqualTo(expected.getRequest());

        assertThat(actual.getInitiator())
                .isEqualTo(1L);

        assertThat(actual.getModule())
                .isEqualTo("audit-module");

        assertThat(actual.getFlows())
                .hasSize(1);

        assertThat(actual.getFlows())
                .extracting("error", String.class)
                .containsExactly("some error");

        AuditFlow flow = actual.getFlows().get(0);

        assertThat(flow.getHeaders())
                .hasSize(1);

        assertThat(flow.getHeaders())
                .extracting(AuditHeader::getKey, AuditHeader::getValue)
                .containsSequence(tuple("hk", "hv"));

        assertThat(flow.getChanges())
                .hasSize(1);

        assertThat(flow.getChanges())
                .extracting(AuditChange::getKey, AuditChange::getOldValue, AuditChange::getNewValue)
                .containsSequence(tuple("k", "o", "n"));
    }

    private static <R> R $(Function<EntityManager, R> fun) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tr = em.getTransaction();
        tr.begin();
        try {
            R ret = fun.apply(em);
            tr.commit();
            return ret;
        } catch (RuntimeException e){
            tr.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    private static <R> R $$(Function<EntityManager, R> fun) {
        EntityManager em = emf.createEntityManager();
        try {
            return fun.apply(em);
        } finally {
            em.close();
        }
    }
}
