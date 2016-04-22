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
package audit.jms.unit;

import javaee.samples.frameworks.injection.InjectionRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.jms.*;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@Vetoed
@RunWith(InjectionRunner.class)
public class FundamentalRequestReply4Test {
    private final CountDownLatch synchronizer = new CountDownLatch(1);

    @Resource(mappedName = "java:/ConnectionFactory")
    ConnectionFactory connectionFactory;

    @Inject
    JMSContext ctx;

    @Resource(mappedName = "java:jms/queue/requestQueue")
    Queue requestQueue;

    @Test
    public void shouldReplyUpdatedMessage() throws Exception {
        new Thread(this::receiver)
                .start();

        TextMessage msg = ctx.createTextMessage("Hi There!");
        TemporaryQueue replyQueue = ctx.createTemporaryQueue();
        msg.setJMSReplyTo(replyQueue);

        ctx.createProducer()
                .send(requestQueue, msg);

        ctx.createConsumer(replyQueue)
                .setMessageListener(m -> {
                    try {
                        if (m instanceof TextMessage) {
                            TextMessage replyMessage = (TextMessage) m;
                            assertThat(replyMessage.getText())
                                    .isEqualTo("Hi There! replied back to sender");
                        } else {
                            fail("illegal message type");
                        }
                    } catch (JMSException e) {
                        throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
                    } finally {
                        synchronizer.countDown();
                    }
                });

        synchronizer.await(3, SECONDS);
    }

    private void receiver() {
        ctx.createConsumer(requestQueue)
                .setMessageListener(msg -> {
                    try {
                        if (msg instanceof TextMessage) {
                            TextMessage requestMessage = (TextMessage) msg;

                            String contents = requestMessage.getText();

                            assertThat(contents)
                                    .isEqualTo("Hi There!");

                            TextMessage replyMessage = ctx.createTextMessage(contents + " replied back to sender");
                            replyMessage.setJMSCorrelationID(msg.getJMSMessageID());

                            Destination replyDestination = msg.getJMSReplyTo();

                            ctx.createProducer()
                                    .send(replyDestination, replyMessage);
                        } else {
                            fail("illegal message type");
                        }
                    } catch (JMSException e) {
                        fail(e.getLocalizedMessage());
                    }
                });
    }
}
