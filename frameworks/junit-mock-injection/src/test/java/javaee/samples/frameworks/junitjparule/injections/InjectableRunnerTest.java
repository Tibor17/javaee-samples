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
package javaee.samples.frameworks.junitjparule.injections;

import javaee.samples.frameworks.junitjparule.InjectionRunner;
import javaee.samples.frameworks.junitjparule.injections.internal.WithProtectedConstructorNoParameter;
import javaee.samples.frameworks.junitjparule.injections.internal.WithProtectedConstructorParameter;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(InjectionRunner.class)
public class InjectableRunnerTest {
  public interface Int {}

  public static class A {
  }

  public static class B {
    @Inject
    private A a;

    @Inject
    private Int i;

    @Inject
    private C c;

    B() {
    }

    public A getA() {
      return a;
    }
  }

  public static class C {
    @Inject
    private B b;
  }

  public static class D {
    A a;
    C c;

    @Inject
    public D(A a, C c) {
      this.a = a;
      this.c = c;
    }
  }

  public static class E {
  }

  @Produces
  private A a = new A();

  @Inject
  private B b;

  @Inject
  private D d;

  @Inject
  private E e;

  @Inject
  private WithProtectedConstructorNoParameter pro;

  @Inject
  private WithProtectedConstructorParameter proParam;

  @Produces
  private final Int i = new Int() {};

  @Test
  public void test() throws Exception {
    assertThat(b, notNullValue());
    assertThat(b.a, is(sameInstance(a)));

    assertThat(b.i, notNullValue());
    assertThat(b.i, is(sameInstance(i)));

    assertThat(d, notNullValue());
    assertThat(d.a, is(a));
    assertThat(d.c, is(b.c));

    assertThat(pro, is(notNullValue()));
    assertThat(pro.getA(), is(sameInstance(a)));

    assertThat(proParam, is(notNullValue()));
    assertThat(proParam.getA(), is(sameInstance(a)));
  }
}
