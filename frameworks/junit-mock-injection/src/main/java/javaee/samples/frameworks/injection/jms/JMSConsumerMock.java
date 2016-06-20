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
package javaee.samples.frameworks.injection.jms;

import javax.jms.*;

import static javaee.samples.frameworks.injection.jms.Util.castTo;

public final class JMSConsumerMock implements JMSConsumer {
    private final MessageConsumer consumer;

    public JMSConsumerMock(MessageConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public String getMessageSelector() {
        try {
            return consumer.getMessageSelector();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public MessageListener getMessageListener() throws JMSRuntimeException {
        try {
            return consumer.getMessageListener();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSRuntimeException {
        try {
            consumer.setMessageListener(new JMS20MessageListenerDecorator<>(listener));
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public Message receive() {
        try {
            return consumer.receive();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public Message receive(long timeout) {
        try {
            return consumer.receive(timeout);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public Message receiveNoWait() {
        try {
            return consumer.receiveNoWait();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public void close() {
        try {
            consumer.close();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public <T> T receiveBody(Class<T> c) {
        try {
            return castTo(c, consumer.receive());
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public <T> T receiveBody(Class<T> c, long timeout) {
        try {
            return castTo(c, consumer.receive(timeout));
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public <T> T receiveBodyNoWait(Class<T> c) {
        try {
            return castTo(c, consumer.receiveNoWait());
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }
}
