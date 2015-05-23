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

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.persistence.EntityManager;

import static javaee.samples.frameworks.junitjparule.JPARuleBuilder.unitName;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PerClassTest {
    @Rule
    public final JPARule rule = unitName("containerless-test-pu")
            .perClass()
            .build();

    private static EntityManager em;

    @Test
    public void firstOpenExtendedEntityManager() {
        em = rule.createEntityManager();
        assertNotNull(em);
        assertTrue(em.isOpen());
    }

    @Test
    public void secondCheckStillOpenExtendedEntityManager() {
        assertNotNull(em);
        assertTrue(em.isOpen());
    }
}
