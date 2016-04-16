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
package audit.jms.it;

import audit.domain.Audit;
import audit.domain.AuditFlow;
import audit.jms.consumer.AuditMessagingConsumerService;
import audit.jms.producer.AuditMessagingProducerService;
import audit.jms.unit.AuditStorageListener;
import audit.persistence.service.AuditService;
import javaee.samples.frameworks.injection.DatabaseConfiguration;
import javaee.samples.frameworks.injection.InjectionPointOrdinal;
import javaee.samples.frameworks.injection.InjectionRunner;
import javaee.samples.frameworks.injection.WithManagedTransactions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.persistence.PersistenceContext;

import java.util.List;
import java.util.concurrent.CyclicBarrier;

import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javaee.samples.frameworks.injection.DB.UNDEFINED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Vetoed
@RunWith(InjectionRunner.class)
@DatabaseConfiguration(UNDEFINED)
@PersistenceContext(unitName = "audit-jpa")
@WithManagedTransactions
public class JmsPersistenceIT {

    @Produces
    CyclicBarrier synchronizer = new CyclicBarrier(2);

    @Inject
    @InjectionPointOrdinal(1)
    AuditStorageListener listener;

    @Inject
    @InjectionPointOrdinal(2)
    AuditMessagingConsumerService consumerService;

    @Inject
    AuditMessagingProducerService producerService;

    @Inject
    AuditService repository;

    @Before
    public void deleteDatabase() {
        repository.findAll()
                .forEach(repository::remove);
    }

    @Test
    public void shouldValidateJmsDispatch() throws Exception {
        assertThat(repository.findAll())
                .isEmpty();

        Audit original = new Audit();
        original.setRequest(randomUUID());
        original.setInitiator(5);
        original.setModule("test");
        original.setOperationKey("login");
        original.setDescription("desc");
        original.getFlows()
                .add(new AuditFlow());

        producerService.send(original);
        synchronizer.await(3, SECONDS);

        List<Audit> database = repository.findAll();

        assertThat(database)
                .hasSize(1)
                .extracting(Audit::getModule, Audit::getDescription, Audit::getOperationKey, Audit::getInitiator, Audit::getRequest)
                .contains(tuple(original.getModule(), original.getDescription(), original.getOperationKey(), original.getInitiator(), original.getRequest()));

        assertThat(database.get(0).getFlows())
                .hasSize(1);
    }
}
