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
import java.io.Serializable;

public final class JMSContextMock implements JMSContext {
    private final ConnectionFactory factory;

    public JMSContextMock(ConnectionFactory factory) {
        this.factory = factory;
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        return null;
    }

    @Override
    public JMSProducer createProducer() {
        return new JMSProducerMock(factory);
    }

    @Override
    public String getClientID() {
        return null;
    }

    @Override
    public void setClientID(String clientID) {
        throw new IllegalStateRuntimeException("The JMSContext is container-managed (injected).");
    }

    @Override
    public ConnectionMetaData getMetaData() {
        return null;
    }

    @Override
    public ExceptionListener getExceptionListener() {
        return null;
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) {
        throw new IllegalStateRuntimeException("The JMSContext is container-managed (injected).");
    }

    @Override
    public void start() {
        throw new IllegalStateRuntimeException("The JMSContext is container-managed (injected).");
    }

    @Override
    public void stop() {
        throw new IllegalStateRuntimeException("The JMSContext is container-managed (injected).");
    }

    @Override
    public void setAutoStart(boolean autoStart) {
        throw new IllegalStateRuntimeException("The JMSContext is container-managed (injected).");
    }

    @Override
    public boolean getAutoStart() {
        return false;
    }

    @Override
    public void close() {
        throw new IllegalStateRuntimeException("The JMSContext is container-managed (injected).");
    }

    @Override
    public BytesMessage createBytesMessage() {
        return null;
    }

    @Override
    public MapMessage createMapMessage() {
        return null;
    }

    @Override
    public Message createMessage() {
        return null;
    }

    @Override
    public ObjectMessage createObjectMessage() {
        return null;
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) {
        return null;
    }

    @Override
    public StreamMessage createStreamMessage() {
        return null;
    }

    @Override
    public TextMessage createTextMessage() {
        return null;
    }

    @Override
    public TextMessage createTextMessage(String text) {
        return null;
    }

    @Override
    public boolean getTransacted() {
        return false;
    }

    @Override
    public int getSessionMode() {
        return 0;
    }

    @Override
    public void commit() {
        throw new IllegalStateRuntimeException("The JMSContext is container-managed (injected).");
    }

    @Override
    public void rollback() {
        throw new IllegalStateRuntimeException("The JMSContext is container-managed (injected).");
    }

    @Override
    public void recover() {
        throw new IllegalStateRuntimeException("The JMSContext is container-managed (injected).");
    }

    @Override
    public JMSConsumer createConsumer(Destination destination) {
        return null;
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector) {
        return null;
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) {
        return null;
    }

    @Override
    public Queue createQueue(String queueName) {
        return null;
    }

    @Override
    public Topic createTopic(String topicName) {
        return null;
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name) {
        return null;
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) {
        return null;
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name) {
        return null;
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) {
        return null;
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) {
        return null;
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) {
        return null;
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) {
        return null;
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) {
        return null;
    }

    @Override
    public TemporaryQueue createTemporaryQueue() {
        return null;
    }

    @Override
    public TemporaryTopic createTemporaryTopic() {
        return null;
    }

    @Override
    public void unsubscribe(String name) {

    }

    @Override
    public void acknowledge() {

    }
}
