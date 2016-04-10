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
import javaee.samples.frameworks.injection.dao.DaoWithConstructor;
import javaee.samples.frameworks.injection.entities.MyEntity;
import javaee.samples.frameworks.injection.service.Service;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import static javaee.samples.frameworks.injection.JPARuleBuilder.unitName;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(InjectionRunner.class)
public class ServiceTest {

  @Rule
  public final JPARule jpa = unitName("containerless-test-pu")
          .build();

  @Produces
  private final DaoWithConstructor dao = new DaoWithConstructor(jpa.getEntityManager());

  @Inject
  private Service service;

  @Test
  public void shouldInjectProxyEntityManagerToDAO() {
    EntityManager em = jpa.getEntityManager();
    Class<?> emType = EntityManager.class;
    assertThat(em.getClass(), is(not(sameInstance(emType))));

    em.getTransaction().begin();
    MyEntity expected = new MyEntity();
    expected.setName("John Smith");
    service.persist(expected);
    em.getTransaction().commit();

    long id = expected.getId();

    em = jpa.getEntityManagerFactory().createEntityManager();
    MyEntity actual = em.find(MyEntity.class, id);
    assertThat(actual.getId(), is(expected.getId()));
    assertThat(actual.getName(), is(expected.getName()));

    actual = em.createNamedQuery("myentity.byName", MyEntity.class)
        .setParameter("name", "John Smith")
        .getSingleResult();
    assertThat(actual.getId(), is(expected.getId()));
    assertThat(actual.getName(), is(expected.getName()));
  }

}
