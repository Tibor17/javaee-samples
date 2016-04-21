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

import javaee.samples.frameworks.injection.jms.JMSContextMock;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.getProperty;
import static javaee.samples.frameworks.injection.spi.JMSSocketResolverUtils.resolveJMSConnection;

public enum JMSResourceCtx {
    CTX;

    private static final String SOCKET =
            resolveJMSConnection(getProperty("jms.broker.socket"), "tcp://localhost:61616");

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, Queue> queues = new ConcurrentHashMap<>();
    private final Map<String, Topic> topics = new ConcurrentHashMap<>();
    private volatile ConnectionFactory factory;
    private volatile JMSContext jmsContext;
    private volatile BrokerService broker;

    public ConnectionFactory getConnectionFactory() {
        lock.readLock()
                .lock();
        try {
            return factory;
        } finally {
            lock.readLock()
                    .unlock();
        }
    }

    public JMSContext getJmsContext() {
        lock.readLock()
                .lock();
        try {
            return jmsContext;
        } finally {
            lock.readLock()
                    .unlock();
        }
    }

    public Map<String, Queue> getQueues() {
        lock.readLock()
                .lock();
        try {
            return queues;
        } finally {
            lock.readLock()
                    .unlock();
        }
    }

    public Map<String, Topic> getTopics() {
        lock.readLock()
                .lock();
        try {
            return topics;
        } finally {
            lock.readLock()
                    .unlock();
        }
    }

    public void startupJMSCtx() {
        if (jmsContext == null) {
            lock.writeLock()
                    .lock();
            try {
                if (jmsContext != null)
                    return;
                broker = new BrokerService();
                broker.setDeleteAllMessagesOnStartup(true);
                broker.setPersistent(false);
                broker.setUseJmx(false);
                broker.addConnector(SOCKET);
                broker.start();
                String url = SOCKET;
                url += url.contains("?") ? "&" : "?";
                url += "jms.redeliveryPolicy.maximumRedeliveries=1&jms.redeliveryPolicy.initialRedeliveryDelay=0";
                factory = new ActiveMQConnectionFactory(url);
                jmsContext = new JMSContextMock(factory);
                Runnable hook = () -> closeBroker(broker);
                getRuntime().addShutdownHook(new Thread(hook));
            } catch (Exception e) {
                throw new IllegalStateException(e.getLocalizedMessage(), e);
            } finally {
                lock.writeLock()
                        .unlock();
            }
        }
    }

    private static void closeBroker(BrokerService broker) {
        try {
            broker.stop();
        } catch (Exception e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }
}
