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

import audit.persistence.service.AuditService;
import javaee.samples.frameworks.injection.DatabaseConfiguration;
import javaee.samples.frameworks.injection.InjectionRunner;
import javaee.samples.frameworks.injection.WithManagedTransactions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.jms.*;
import javax.persistence.PersistenceContext;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javaee.samples.frameworks.injection.DB.UNDEFINED;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@Vetoed
@RunWith(InjectionRunner.class)
/*@DatabaseConfiguration(UNDEFINED)
@PersistenceContext(unitName = "audit-jpa")
@WithManagedTransactions*/
public class JmsReplyPersistenceIT {
    private final CountDownLatch synchronizer = new CountDownLatch(1);

    @Resource(mappedName = "java:/ConnectionFactory")
    ConnectionFactory connectionFactory;

    @Inject
    JMSContext ctx;

    /*@Inject
    AuditService repository;*/

    @Resource(mappedName = "java:jms/queue/requestQueue")
    Queue requestQueue;

    @Before
    public void deleteDatabase() {
        /*repository.findAll()
                .forEach(repository::remove);*/
    }

    @Test
    public void shouldReplyUpdatedMessage() throws Exception {
    }
}
