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

import javaee.samples.frameworks.junitjparule.entities.MyEntity;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static javaee.samples.frameworks.junitjparule.JPARuleBuilder.unitName;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RuleTest {

  @Rule
  public final JPARule jpa = unitName("containerless-test-pu").build();

  @Test
  public void shouldRestore() {
    EntityManager em = jpa.getEntityManagerFactory().createEntityManager();
    em.getTransaction().begin();
    MyEntity expected = new MyEntity();
    expected.setName("John Smith");
    em.persist(expected);
    long id = expected.getId();
    em.getTransaction().commit();

    em = jpa.getEntityManagerFactory().createEntityManager();
    MyEntity actual = em.find(MyEntity.class, id);
    assertThat(actual.getId(), is(expected.getId()));
    assertThat(actual.getName(), is(expected.getName()));

    em = jpa.getEntityManagerFactory().createEntityManager();
    actual = em.createNamedQuery("myentity.byName", MyEntity.class)
        .setParameter("name", "John Smith")
        .getSingleResult();
    assertThat(actual.getId(), is(expected.getId()));
    assertThat(actual.getName(), is(expected.getName()));
  }

  @Test
  @Transactional
  public void shouldRestoreTransactional() {
    MyEntity entity = new MyEntity();
    entity.setName("John Smith");
    assertThat(entity.getId(), is(nullValue()));
    jpa.getCurrentEntityManager().persist(entity);
    Long id = (Long) jpa.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
    assertThat(id, is(notNullValue()));
    assertThat(id, is(not(0L)));
  }

  @Test
  public void shouldRestoreBlocks() {

    // this is a @Transactional block; closing Persistence context by return
    // After EM was closed, jpa.getCurrentEntityManager() returns new isolated instance.
    MyEntity expected = jpa.$$(em -> {
      MyEntity e = new MyEntity();
      e.setName("John Smith");
      em.persist(e);
      return e;
    });

    long id = expected.getId();
    MyEntity actual = jpa.getCurrentEntityManager().find(MyEntity.class, id);
    assertThat(actual, is(notNullValue()));
    assertThat(actual.getId(), is(expected.getId()));
    assertThat(actual.getName(), is(expected.getName()));

    actual = jpa.getCurrentEntityManager()
        .createNamedQuery("myentity.byName", MyEntity.class)
        .setParameter("name", "John Smith")
        .getSingleResult();

    assertThat(actual.getId(), is(expected.getId()));
    assertThat(actual.getName(), is(expected.getName()));
  }
}
