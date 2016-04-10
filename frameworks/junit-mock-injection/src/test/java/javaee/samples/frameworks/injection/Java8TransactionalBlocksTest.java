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

import javaee.samples.frameworks.injection.entities.MyEntity;
import org.junit.Rule;
import org.junit.Test;

import javax.persistence.EntityManager;

import static javaee.samples.frameworks.injection.JPARuleBuilder.unitName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class Java8TransactionalBlocksTest {

  @Rule
  public final JPARule jpa = unitName("containerless-test-pu").build();

  @Test
  public void callableInJava8() {
    MyEntity expected = jpa.$$(em -> {
      MyEntity e = new MyEntity();
      e.setName("John Smith");
      em.persist(e);
      return e;
    });

    long id = expected.getId();
    MyEntity actual = jpa.getCurrentEntityManager().find(MyEntity.class, id);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void commitableInJava8() {

    MyEntity expected = jpa.$(() -> {
      MyEntity e = new MyEntity();
      e.setName("John Smith");
      EntityManager em = jpa.getCurrentEntityManager();
      em.persist(e);
      return e;
    });

    long id = expected.getId();
    MyEntity actual = jpa.getCurrentEntityManager().find(MyEntity.class, id);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void transactionInJava8() {

    jpa.$(em -> {
      MyEntity e = new MyEntity();
      e.setName("John Smith");
      em.persist(e);
    });

    MyEntity actual = jpa.getCurrentEntityManager().find(MyEntity.class, 1L);
    assertThat(actual, is(notNullValue()));
  }
}
