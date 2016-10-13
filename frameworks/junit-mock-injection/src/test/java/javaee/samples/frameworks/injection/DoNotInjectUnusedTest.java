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

import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Injection point non-null reference not mandatory.
 */
@RunWith(InjectionRunner.class)
public class DoNotInjectUnusedTest {
    public interface I1 {}
    public interface I2 {}
    public class C2 implements I2 {}
    public static class S1 { @Inject I1 i1; @Inject I2 i2; }
    public static class S2 {
        I1 i1;
        I2 i2;
        boolean constructorCalled;

        @Inject
        S2(I1 i1, I2 i2) {
            this.i1 = i1;
            this.i2 = i2;
            constructorCalled = true;
        }
    }

    @Inject
    I1 i1;

    @Inject
    I2 i2;

    @Produces
    I2 provided = new I2() {};

    @Inject
    S1 s1;

    @Inject
    S2 s2;

    @Test
    public void shouldInjectI2Only() {
        assertThat(i1).isNull();
        assertThat(i2).isNotNull();
        assertThat(s1).isNotNull();
        assertThat(s1.i1).isNull();
        assertThat(s1.i2).isNotNull();
        assertThat(s2).isNotNull();
        assertThat(s2.constructorCalled).isTrue();
        assertThat(s2.i1).isNull();
        assertThat(s2.i2).isNotNull();
    }
}
