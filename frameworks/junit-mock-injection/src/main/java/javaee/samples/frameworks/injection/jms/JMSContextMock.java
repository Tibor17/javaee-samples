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
import java.lang.IllegalStateException;

import static java.lang.reflect.Proxy.newProxyInstance;

public final class JMSContextMock implements JMSContext {
    private final Connection connection;
    private final Session session;

    private static class SessionThreadLocal extends ThreadLocal<Session> {
        private final boolean transacted;
        private final int acknowledgeMode;
        private final Connection connection;

        private SessionThreadLocal(boolean transacted, int acknowledgeMode, Connection connection) {
            this.transacted = transacted;
            this.acknowledgeMode = acknowledgeMode;
            this.connection = connection;
        }

        @Override
        protected Session initialValue() {
            try {
                return connection.createSession(transacted, acknowledgeMode);
            } catch (JMSException e) {
                throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
            }
        }
    }

    public JMSContextMock(ConnectionFactory factory, String clientId, boolean transacted, int acknowledgeMode) {
        try {
            connection = factory.createConnection();
            clientId = clientId == null ? null : clientId.trim();
            if (clientId != null && !clientId.isEmpty()) {
                connection.setClientID(clientId);
            }
            connection.start();
            final ThreadLocal<Session> ls = new SessionThreadLocal(transacted, acknowledgeMode, connection);
            Class<?>[] sType = {Session.class};
            ClassLoader cl = context();
            session = (Session) newProxyInstance(cl, sType, (proxy, method, args) -> method.invoke(ls.get(), args));
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public Session getSession() {
        return session;
    }

    private static ClassLoader context() {
        return Thread.currentThread().getContextClassLoader();
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        throw new JMSRuntimeException("The method JMSContext.createContext() " +
                "is being called in a Java EE web or EJB application.");
    }

    @Override
    public JMSProducer createProducer() {
        return new JMSProducerMock(session);
    }

    @Override
    public String getClientID() {
        try {
            return connection.getClientID();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public void setClientID(String clientID) {
    }

    @Override
    public ConnectionMetaData getMetaData() {
        try {
            return connection.getMetaData();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public ExceptionListener getExceptionListener() {
        return null;
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void setAutoStart(boolean autoStart) {
    }

    @Override
    public boolean getAutoStart() {
        return true;
    }

    @Override
    public void close() {
    }

    public void closeConnection() {
        try {
            session.close();
            connection.close();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public BytesMessage createBytesMessage() {
        try {
            return session.createBytesMessage();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public MapMessage createMapMessage() {
        try {
            return session.createMapMessage();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public Message createMessage() {
        try {
            return session.createMessage();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public ObjectMessage createObjectMessage() {
        try {
            return session.createObjectMessage();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) {
        try {
            return session.createObjectMessage(object);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public StreamMessage createStreamMessage() {
        try {
            return session.createStreamMessage();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public TextMessage createTextMessage() {
        try {
            return session.createTextMessage();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public TextMessage createTextMessage(String text) {
        try {
            return session.createTextMessage(text);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public boolean getTransacted() {
        return false;
    }

    @Override
    public int getSessionMode() {
        try {
            return session.getAcknowledgeMode();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public void commit() {
        try {
            session.commit();
        } catch (IllegalStateException e) {
            throw new IllegalStateRuntimeException(e.getLocalizedMessage());
        } catch (TransactionRolledBackException e) {
            throw new TransactionRolledBackRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rollback() {
        try {
            session.rollback();
        } catch (IllegalStateException e) {
            throw new IllegalStateRuntimeException(e.getLocalizedMessage());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recover() {
        try {
            session.rollback();
        } catch (IllegalStateException e) {
            throw new IllegalStateRuntimeException(e.getLocalizedMessage());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JMSConsumer createConsumer(Destination destination) {
        try {
            return new JMSConsumerMock(session.createConsumer(destination));
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
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
        try {
            return session.createQueue(queueName);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public Topic createTopic(String topicName) {
        try {
            return session.createTopic(topicName);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name) {
        try {
            return new JMSConsumerMock(session.createDurableConsumer(topic, name));
        } catch (InvalidDestinationException e) {
            throw new InvalidDestinationRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) {
        try {
            return new JMSConsumerMock(session.createDurableConsumer(topic, name, messageSelector, noLocal));
        } catch (InvalidDestinationException e) {
            throw new InvalidDestinationRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (InvalidSelectorException e) {
            throw new InvalidSelectorRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name) {
        try {
            return new JMSConsumerMock(session.createSharedDurableConsumer(topic, name));
        } catch (InvalidDestinationException e) {
            throw new InvalidDestinationRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) {
        try {
            return new JMSConsumerMock(session.createSharedDurableConsumer(topic, name, messageSelector));
        } catch (InvalidDestinationException e) {
            throw new InvalidDestinationRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (InvalidSelectorException e) {
            throw new InvalidSelectorRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) {
        try {
            return new JMSConsumerMock(session.createSharedConsumer(topic, sharedSubscriptionName));
        } catch (InvalidDestinationException e) {
            throw new InvalidDestinationRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (InvalidSelectorException e) {
            throw new InvalidSelectorRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) {
        try {
            return new JMSConsumerMock(session.createSharedConsumer(topic, sharedSubscriptionName, messageSelector));
        } catch (InvalidDestinationException e) {
            throw new InvalidDestinationRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (InvalidSelectorException e) {
            throw new InvalidSelectorRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) {
        try {
            return session.createBrowser(queue);
        } catch (InvalidDestinationException e) {
            throw new InvalidDestinationRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) {
        try {
            return session.createBrowser(queue, messageSelector);
        } catch (InvalidDestinationException e) {
            throw new InvalidDestinationRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (InvalidSelectorException e) {
            throw new InvalidSelectorRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public TemporaryQueue createTemporaryQueue() {
        try {
            return session.createTemporaryQueue();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public TemporaryTopic createTemporaryTopic() {
        try {
            return session.createTemporaryTopic();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public void unsubscribe(String name) {
        try {
            session.unsubscribe(name);
        } catch (InvalidDestinationException e) {
            throw new InvalidDestinationRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getLocalizedMessage(), e.getErrorCode(), e);
        }
    }

    @Override
    public void acknowledge() {
    }
}
