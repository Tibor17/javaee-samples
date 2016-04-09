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
import audit.jms.consumer.AuditListener;
import audit.jms.consumer.AuditMessagingConsumerService;
import audit.jms.producer.AuditMessagingProducerService;
import javaee.samples.frameworks.junitjparule.InjectionRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;

import java.util.concurrent.CyclicBarrier;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(InjectionRunner.class)
public class JmsCommunicationTest {

    private final CyclicBarrier synchronizer = new CyclicBarrier(2);

    @Resource(mappedName = "java:/ConnectionFactory")
    ConnectionFactory connectionFactory;

    @Produces
    AuditListener listener = new Listener(synchronizer);

    @Inject
    AuditMessagingConsumerService consumerService;

    @Inject
    AuditMessagingProducerService producerService;

    @Test
    public void test() throws Exception {
        assertThat(connectionFactory)
                .isNotNull();

        Audit audit = new Audit();
        audit.setModule("test");

        producerService.send(audit);
        synchronizer.await(3, SECONDS);
    }
}
