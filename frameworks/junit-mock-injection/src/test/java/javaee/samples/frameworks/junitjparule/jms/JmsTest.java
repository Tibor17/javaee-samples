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
package javaee.samples.frameworks.junitjparule.jms;

import javaee.samples.frameworks.junitjparule.InjectionRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.*;
import java.util.concurrent.CyclicBarrier;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(InjectionRunner.class)
public class JmsTest {

    private final CyclicBarrier synchronizer = new CyclicBarrier(2);

    @Resource(mappedName = "java:/ConnectionFactory")
    ConnectionFactory connectionFactory;

    @Inject
    Sender sender;

    @Inject
    Receiver receiver;

    @Before
    public void injectSynchronizer() {
        receiver.synchronizer = synchronizer;
    }

    @Test
    public void shouldReceiveExpectedMessage() throws Exception {
        assertThat(connectionFactory)
                .isNotNull();

        sender.send("Test Message");
        synchronizer.await(3, SECONDS);
    }
}
