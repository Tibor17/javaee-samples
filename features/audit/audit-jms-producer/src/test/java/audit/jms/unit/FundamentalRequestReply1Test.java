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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.enterprise.inject.Vetoed;
import javax.jms.*;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@Vetoed
@RunWith(InjectionRunner.class)
public class FundamentalRequestReply1Test {

    @Resource(mappedName = "java:/ConnectionFactory")
    ConnectionFactory connectionFactory;

    @Resource(mappedName = "java:jms/queue/replyQueue")
    Queue replyQueue;

    @Resource(mappedName = "java:jms/queue/requestQueue")
    Queue requestQueue;

    @Resource(mappedName = "java:jms/queue/invalidQueue")
    Queue invalidQueue;

    volatile Connection reqConn, repConn;

    @Before
    public void startJMSConnections() throws JMSException {
        reqConn = connectionFactory.createConnection();
        repConn = connectionFactory.createConnection();
    }

    @After
    public void stopJMSConnection() throws JMSException {
        reqConn.close();
        repConn.close();
    }

    @Test
    public void shouldExperiment() throws Exception {
        new Thread(this::receiver).start();

        Session session = reqConn.createSession(false, AUTO_ACKNOWLEDGE);

        MessageProducer requestProducer = session.createProducer(requestQueue);
        MessageConsumer replyConsumer = session.createConsumer(replyQueue);

        TextMessage requestMessage = session.createTextMessage("Hi There!");
        requestMessage.setJMSReplyTo(replyQueue);
        reqConn.start();
        requestProducer.send(requestMessage);

        Message msg = replyConsumer.receive();
        if (msg instanceof TextMessage) {
            TextMessage replyMessage = (TextMessage) msg;
            assertThat(replyMessage.getText())
                    .isEqualTo("Hi There! replied back to sender");
        } else {
            MessageProducer invalidProducer = session.createProducer(invalidQueue);
            msg.setJMSCorrelationID(msg.getJMSMessageID());
            invalidProducer.send(msg);
            fail("illegal message type");
        }
    }

    private void receiver() {
        try {
            Session session = repConn.createSession(false, AUTO_ACKNOWLEDGE);

            MessageConsumer requestConsumer = session.createConsumer(requestQueue);
            requestConsumer.setMessageListener(m -> {
                try {
                    if (m instanceof TextMessage) {
                        TextMessage requestMessage = (TextMessage) m;
                        assertThat(requestMessage.getText())
                                .isEqualTo("Hi There!");

                        String contents = requestMessage.getText();
                        Destination replyDestination = requestMessage.getJMSReplyTo();
                        MessageProducer replyProducer = session.createProducer(replyDestination);

                        TextMessage replyMessage = session.createTextMessage(contents + " replied back to sender");
                        replyMessage.setJMSCorrelationID(requestMessage.getJMSMessageID());
                        replyProducer.send(replyMessage);
                    } else {
                        MessageProducer invalidProducer = session.createProducer(invalidQueue);
                        m.setJMSCorrelationID(m.getJMSMessageID());
                        invalidProducer.send(m);
                        fail("illegal message type");
                    }
                } catch (JMSException e) {
                    fail(e.getLocalizedMessage());
                }
            });
            repConn.start();
        } catch (JMSException e) {
            fail(e.getLocalizedMessage());
        }
    }
}
