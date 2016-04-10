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
package javaee.samples.frameworks.injection.jpa;

import javaee.samples.frameworks.injection.InjectionRunner;
import javaee.samples.frameworks.injection.JPARule;
import javaee.samples.frameworks.injection.dao.DAO;
import javaee.samples.frameworks.injection.entities.MyEntity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static javaee.samples.frameworks.injection.JPARuleBuilder.unitName;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(InjectionRunner.class)
public class EMProxyTest {

  @Rule
  public final JPARule jpa = unitName("containerless-test-pu")
          .build();

  @Inject
  private DAO dao;

  @Test
  public void shouldAccessRealEntityManagerFromProxy() {
    EntityManager em = jpa.getEntityManager();
    assertThat(em, is(not(sameInstance(EntityManager.class))));

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
  public void shouldInjectProxyEntityManagerToDAO() {
    EntityManager em = jpa.getEntityManager();
    assertThat(em, is(not(sameInstance(EntityManager.class))));

    em.getTransaction().begin();
    MyEntity expected = new MyEntity();
    expected.setName("John Smith");
    dao.persist(expected);
    em.getTransaction().commit();

    long id = expected.getId();

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
}
