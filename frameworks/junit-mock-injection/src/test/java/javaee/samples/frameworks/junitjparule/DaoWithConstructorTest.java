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

import javaee.samples.frameworks.junitjparule.dao.DaoWithConstructor;
import javaee.samples.frameworks.junitjparule.entities.MyEntity;
import org.junit.Rule;
import org.junit.Test;

import javax.persistence.EntityManager;

import static javaee.samples.frameworks.junitjparule.JPARuleBuilder.unitName;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class DaoWithConstructorTest {
    @Rule
    public final JPARule rule = unitName("containerless-test-pu")
            .build();

    private EntityManager em = rule.getEntityManager();

    private final DaoWithConstructor dao = new DaoWithConstructor(em);

    @Test
    public void shouldReloadWithPureEntityManager() {
        em.getTransaction().begin();
        MyEntity expected = new MyEntity();
        expected.setName("John Smith");
        dao.persist(expected);
        em.getTransaction().commit();
        rule.getCurrentEntityManager().close();

        long id = expected.getId();
        assertThat(id, is(not(0)));
        assertThat(id, is(greaterThan(0L)));

        em = rule.getEntityManagerFactory().createEntityManager();
        MyEntity actual1 = em.find(MyEntity.class, id);
        assertNotNull(actual1);
        assertNotSame(expected, actual1);
        assertThat(actual1.getId(), is(expected.getId()));
        assertThat(actual1.getName(), is(expected.getName()));

        em = rule.getEntityManagerFactory().createEntityManager();
        MyEntity actual2 = em.createNamedQuery("myentity.byName", MyEntity.class)
                .setParameter("name", "John Smith")
                .getSingleResult();
        assertNotNull(actual2);
        assertNotSame(expected, actual2);
        assertNotSame(actual1, actual2);
        assertThat(actual2.getId(), is(expected.getId()));
        assertThat(actual2.getName(), is(expected.getName()));
        rule.getCurrentEntityManager().close();

        MyEntity actual3 = dao.findByName("John Smith");
        assertNotNull(actual3);
        assertNotSame(expected, actual3);
        assertNotSame(actual2, actual3);
        assertThat(actual3.getId(), is(expected.getId()));
        assertThat(actual3.getName(), is(expected.getName()));
    }

    @Test
    public void shouldReloadWithCurrentEntityManager() {
        MyEntity expected = rule.$$(em -> {
            MyEntity entity = new MyEntity();
            entity.setName("John Smith");
            dao.persist(entity);
            return entity;
        });
        final long id = expected.getId();
        assertNotNull(expected);
        assertThat(id, is(not(0)));
        assertThat(id, is(greaterThan(0L)));

        MyEntity actual1 = rule.getEntityManager().find(MyEntity.class, id);
        assertNotNull(actual1);
        assertNotSame(expected, actual1);
        assertThat(actual1.getId(), is(expected.getId()));
        assertThat(actual1.getName(), is(expected.getName()));
        rule.getCurrentEntityManager().close();

        MyEntity actual2 = rule.getEntityManager()
                .createNamedQuery("myentity.byName", MyEntity.class)
                .setParameter("name", "John Smith")
                .getSingleResult();
        assertNotNull(actual2);
        assertNotSame(expected, actual2);
        assertNotSame(actual1, actual2);
        assertThat(actual2.getId(), is(expected.getId()));
        assertThat(actual2.getName(), is(expected.getName()));
        rule.getCurrentEntityManager().close();

        MyEntity actual3 = dao.findByName("John Smith");
        assertNotNull(actual3);
        assertNotSame(expected, actual3);
        assertNotSame(actual2, actual3);
        assertThat(actual3.getId(), is(expected.getId()));
        assertThat(actual3.getName(), is(expected.getName()));
    }
}
