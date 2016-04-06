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
package javaee.samples.frameworks.junitjparule.transactions;

import javaee.samples.frameworks.junitjparule.entities.MyEntity;
import org.junit.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static javaee.samples.frameworks.junitjparule.Transactions.*;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class Base {
    @Inject
    EntityManager em;

    @Test
    public void shouldStoreUnderTransaction() {
        MyEntity e = new MyEntity();
        e.setName("nm");

        $(em -> em.persist(new MyEntity("xyz")));

        $(em::persist, e);

        e.setName("new name");

        e = $$(em::merge, e);

        assertThat(e.getId())
                .isNotNull();

        final MyEntity e2 = e;
        MyEntity result = $$(em -> em.merge(e2));

        assertThat(result.getId())
                .isNotNull();

        assertThat(result.getId())
                .isEqualTo(e.getId());

        int updatedRows = $(this::update, "MyEntity.updateName", "another name");
        assertThat(updatedRows)
                .isEqualTo(2);

        updatedRows = $(this::update, "MyEntity.updateNameIfId", e.getId(), "again another name");
        assertThat(updatedRows)
                .isEqualTo(1);
    }

    private int update(String query, String param) {
        return em.createNamedQuery(query)
                .setParameter(1, param)
                .executeUpdate();
    }

    private int update(String query, long id, String param) {
        return em.createNamedQuery(query)
                .setParameter(1, param)
                .setParameter(2, id)
                .executeUpdate();
    }
}
