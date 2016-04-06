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
package jms;

import jms.wrappers.JMSBroker;
import jms.wrappers.JMSConsumer;
import jms.wrappers.JMSProducer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.*;
import java.util.concurrent.CountDownLatch;

import static jms.wrappers.JMSConsumer.createConsumerOnQueue;
import static jms.wrappers.JMSProducer.createProducerOnQueue;
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

public class JMSDeliveryTest implements MessageListener, ExceptionListener {
    private final CountDownLatch messageCounter = new CountDownLatch(1);

    private JMSConsumer consumer;
    private JMSProducer<Queue> producer;
    private JMSBroker broker;

    @Before
    public void startBroker() throws Exception {
        broker = new JMSBroker(61617, true);
        broker.start();
    }

    @After
    public void stopBroker() throws Exception {
        consumer.getMessageConsumer().close();
        consumer.getConnection().close();
        producer.getMessageProducer().close();
        producer.getConnection().close();
        broker.stop();
    }

    @Test
    public void shouldSendTextToQueue() throws Exception {
        consumer = createConsumerOnQueue("tcp://127.0.0.1:61617?jms.redeliveryPolicy.maximumRedeliveries=1&jms.redeliveryPolicy.initialRedeliveryDelay=0", "jms/queue/test", of(this));
        producer = createProducerOnQueue("tcp://127.0.0.1:61617", "jms/queue/test");
        producer.getConnection().start();
        TextMessage message = producer.getSession().createTextMessage(" Message ");
        producer.getMessageProducer().send(message);
        messageCounter.await(5, SECONDS);
    }

    @Override
    public void onMessage(Message message) {
        try {
            // todo In JMS 2.0 call message.getBody(String.class)
            TextMessage textMessage = (TextMessage) message;
            assertThat(textMessage.getText())
                    .isEqualTo(" Message ");
            message.acknowledge();
        } catch (JMSException e) {
            fail(e.getLocalizedMessage());
        } finally {
            messageCounter.countDown();
        }
    }

    @Override
    public void onException(JMSException exception) {
        fail("called JMS onException()");
    }
}
