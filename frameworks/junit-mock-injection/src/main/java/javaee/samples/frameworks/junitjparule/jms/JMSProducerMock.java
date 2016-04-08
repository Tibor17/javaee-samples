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

import javax.jms.*;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;

final public class JMSProducerMock implements JMSProducer {
    private final ConnectionFactory factory;

    public JMSProducerMock(ConnectionFactory factory) {
        this.factory = factory;
    }

    @Override
    public JMSProducer send(Destination destination, Message message) {
        try (Connection connection = factory.createConnection()) {
            Session session = connection.createSession(false, AUTO_ACKNOWLEDGE);
            MessageProducer publisher = session.createProducer(destination);
            connection.start();
            publisher.send(message);
            return this;
        } catch (InvalidDestinationException e) {
            throw new InvalidDestinationRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public JMSProducer send(Destination destination, String body) {
        try (Connection connection = factory.createConnection()) {
            Session session = connection.createSession(false, AUTO_ACKNOWLEDGE);
            MessageProducer publisher = session.createProducer(destination);
            connection.start();
            TextMessage msg = session.createTextMessage(body);
            publisher.send(msg);
            return this;
        } catch (InvalidDestinationException e) {
            throw new InvalidDestinationRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public JMSProducer send(Destination destination, Map<String, Object> body) {
        return null;
    }

    @Override
    public JMSProducer send(Destination destination, byte[] body) {
        return null;
    }

    @Override
    public JMSProducer send(Destination destination, Serializable body) {
        try (Connection connection = factory.createConnection()) {
            Session session = connection.createSession(false, AUTO_ACKNOWLEDGE);
            MessageProducer publisher = session.createProducer(destination);
            connection.start();
            ObjectMessage msg = session.createObjectMessage(body);
            publisher.send(msg);
            return this;
        } catch (InvalidDestinationException e) {
            throw new InvalidDestinationRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public JMSProducer setDisableMessageID(boolean value) {
        return null;
    }

    @Override
    public boolean getDisableMessageID() {
        return false;
    }

    @Override
    public JMSProducer setDisableMessageTimestamp(boolean value) {
        return null;
    }

    @Override
    public boolean getDisableMessageTimestamp() {
        return false;
    }

    @Override
    public JMSProducer setDeliveryMode(int deliveryMode) {
        return null;
    }

    @Override
    public int getDeliveryMode() {
        return 0;
    }

    @Override
    public JMSProducer setPriority(int priority) {
        return null;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public JMSProducer setTimeToLive(long timeToLive) {
        return null;
    }

    @Override
    public long getTimeToLive() {
        return 0;
    }

    @Override
    public JMSProducer setDeliveryDelay(long deliveryDelay) {
        return null;
    }

    @Override
    public long getDeliveryDelay() {
        return 0;
    }

    @Override
    public JMSProducer setAsync(CompletionListener completionListener) {
        return null;
    }

    @Override
    public CompletionListener getAsync() {
        return null;
    }

    @Override
    public JMSProducer setProperty(String name, boolean value) {
        return null;
    }

    @Override
    public JMSProducer setProperty(String name, byte value) {
        return null;
    }

    @Override
    public JMSProducer setProperty(String name, short value) {
        return null;
    }

    @Override
    public JMSProducer setProperty(String name, int value) {
        return null;
    }

    @Override
    public JMSProducer setProperty(String name, long value) {
        return null;
    }

    @Override
    public JMSProducer setProperty(String name, float value) {
        return null;
    }

    @Override
    public JMSProducer setProperty(String name, double value) {
        return null;
    }

    @Override
    public JMSProducer setProperty(String name, String value) {
        return null;
    }

    @Override
    public JMSProducer setProperty(String name, Object value) {
        return null;
    }

    @Override
    public JMSProducer clearProperties() {
        return null;
    }

    @Override
    public boolean propertyExists(String name) {
        return false;
    }

    @Override
    public boolean getBooleanProperty(String name) {
        return false;
    }

    @Override
    public byte getByteProperty(String name) {
        return 0;
    }

    @Override
    public short getShortProperty(String name) {
        return 0;
    }

    @Override
    public int getIntProperty(String name) {
        return 0;
    }

    @Override
    public long getLongProperty(String name) {
        return 0;
    }

    @Override
    public float getFloatProperty(String name) {
        return 0;
    }

    @Override
    public double getDoubleProperty(String name) {
        return 0;
    }

    @Override
    public String getStringProperty(String name) {
        return null;
    }

    @Override
    public Object getObjectProperty(String name) {
        return null;
    }

    @Override
    public Set<String> getPropertyNames() {
        return null;
    }

    @Override
    public JMSProducer setJMSCorrelationIDAsBytes(byte[] correlationID) {
        return null;
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() {
        return new byte[0];
    }

    @Override
    public JMSProducer setJMSCorrelationID(String correlationID) {
        return null;
    }

    @Override
    public String getJMSCorrelationID() {
        return null;
    }

    @Override
    public JMSProducer setJMSType(String type) {
        return null;
    }

    @Override
    public String getJMSType() {
        return null;
    }

    @Override
    public JMSProducer setJMSReplyTo(Destination replyTo) {
        return null;
    }

    @Override
    public Destination getJMSReplyTo() {
        return null;
    }
}
