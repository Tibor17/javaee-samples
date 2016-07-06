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

import javaee.samples.frameworks.injection.DatabaseConfiguration;
import javaee.samples.frameworks.injection.InjectionRunner;
import javaee.samples.frameworks.injection.WithManagedTransactions;
import jpa.MyEntity;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static javaee.samples.frameworks.injection.DB.UNDEFINED;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(InjectionRunner.class)
@DatabaseConfiguration(UNDEFINED)
@PersistenceContext(unitName = "pu")
@WithManagedTransactions
public class TimerIT {
    @Inject
    EntityManager em;

    @Test
    public void should() throws InterruptedException {
        TimeUnit.MILLISECONDS
                .sleep(1500);

        Collection<MyEntity> results =
                em.createQuery("select e from MyEntity e", MyEntity.class)
                .getResultList();

        assertThat(results, hasSize(greaterThan(0)));
    }
}
