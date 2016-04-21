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

import static javaee.samples.frameworks.injection.DB.UNDEFINED;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@Vetoed
@RunWith(InjectionRunner.class)
/*@DatabaseConfiguration(UNDEFINED)
@PersistenceContext(unitName = "audit-jpa")
@WithManagedTransactions*/
public class JmsReplyPersistenceIT {

    @Resource(mappedName = "java:/ConnectionFactory")
    ConnectionFactory connectionFactory;

    @Inject
    JMSContext ctx;

    @Resource(mappedName = "java:jms/queue/audit")
    Queue destination;

    /*@Inject
    AuditService repository;*/

    @Resource(mappedName = "java:jms/queue/replyQueue")
    Queue replyQueue;

    @Resource(mappedName = "java:jms/queue/requestQueue")
    Queue requestQueue;

    @Resource(mappedName = "java:jms/queue/invalidQueue")
    Queue invalidQueue;

    @Before
    public void deleteDatabase() {
        /*repository.findAll()
                .forEach(repository::remove);*/
    }

    @Test
    public void test() throws Exception {
        TextMessage msg = ctx.createTextMessage("test");
        TemporaryQueue replyQueue = ctx.createTemporaryQueue();
        msg.setJMSReplyTo(replyQueue);
        ctx.createProducer().send(destination, msg);
        TextMessage received = (TextMessage) ctx.createConsumer(replyQueue).receive(5000);
        System.out.println("sender received message: " + received.getText());
    }
}
