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
package javaee.samples.frameworks.injection.spi;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.getProperty;

public enum JMSResourceCtx {
    CTX;

    private static final String SOCKET = getProperty("jms.broker.socket", "tcp://localhost:61616");

    private final Map<String, Queue> queues = new HashMap<>();
    private final Map<String, Topic> topics = new HashMap<>();
    private final Map<MessageListener, Void> listeners = new HashMap<>();
    private ConnectionFactory factory;
    private JMSContext jmsContext;
    private BrokerService broker;

    public ConnectionFactory getConnectionFactory() {
        return factory;
    }

    public boolean hasConnectionFactory() {
        return factory != null;
    }

    public void setConnectionFactory(ConnectionFactory factory) {
        this.factory = factory;
    }

    public JMSContext getJmsContext() {
        return jmsContext;
    }

    public boolean hasJMSContext() {
        return jmsContext != null;
    }

    public void setJmsContext(JMSContext jmsContext) {
        this.jmsContext = jmsContext;
    }

    public Map<String, Queue> getQueues() {
        return queues;
    }

    public Map<String, Topic> getTopics() {
        return topics;
    }

    public void addListener(MessageListener listener) {
        listeners.putIfAbsent(listener, null);
    }

    public boolean hasListener(MessageListener listener) {
        return listeners.containsKey(listener);
    }

    public ConnectionFactory startConnectionFactory() {
        if (!hasConnectionFactory()) {
            try {
                broker = new BrokerService();
                broker.setDeleteAllMessagesOnStartup(true);
                broker.setPersistent(true);
                broker.setUseJmx(false);
                broker.addConnector(SOCKET);
                broker.start();
                String url = SOCKET;
                url += url.contains("?") ? "&" : "?";
                url += "jms.redeliveryPolicy.maximumRedeliveries=1&jms.redeliveryPolicy.initialRedeliveryDelay=0";
                setConnectionFactory(new ActiveMQConnectionFactory(url));
            } catch (Exception e) {
                throw new IllegalStateException(e.getLocalizedMessage(), e);
            }
        }
        return getConnectionFactory();
    }

    public void closeBroker() {
        if (broker != null) {
            try {
                broker.stop();
            } catch (Exception e) {
                throw new IllegalStateException(e.getLocalizedMessage(), e);
            }
        }
    }
}
