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
import org.apache.activemq.ConnectionClosedException;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.transport.TransportDisposedIOException;

import javax.jms.ConnectionFactory;
import javax.jms.JMSRuntimeException;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.getProperty;
import static javaee.samples.frameworks.injection.spi.JMSSocketResolverUtils.resolveJMSConnection;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public enum JMSResourceCtx {
    CTX;

    private static final String SOCKET =
            resolveJMSConnection(getProperty("jms.broker.socket"), "tcp://localhost:61616");

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, Queue> queues = new ConcurrentHashMap<>();
    private final Map<String, Topic> topics = new ConcurrentHashMap<>();
    private final Deque<JMSContextMock> jmsContext = new ConcurrentLinkedDeque<>();
    private volatile ConnectionFactory factory;
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

    public JMSContextMock startJMSCtx() {
        startBrokerIfAbsent();
        JMSContextMock ctx = new JMSContextMock(factory, null, false, AUTO_ACKNOWLEDGE);
        jmsContext.offer(ctx);
        return ctx;
    }

    public void startBrokerIfAbsent() {
        if (broker == null) {
            lock.writeLock()
                    .lock();
            try {
                if (broker == null) {
                    try {
                        broker = new BrokerService();
                        broker.setDeleteAllMessagesOnStartup(true);
                        broker.setPersistent(false);
                        broker.setUseJmx(false);
                        broker.addConnector(SOCKET);
                        broker.start();
                    } catch (Exception e) {
                        throw new IllegalStateException(e.getLocalizedMessage(), e);
                    }

                    Runnable hook = () -> {
                        try {
                            jmsContext.forEach(JMSContextMock::closeConnection);
                        } catch (JMSRuntimeException e) {
                            if (!(e.getCause() instanceof TransportDisposedIOException)
                                    && !(e.getCause() instanceof ConnectionClosedException)) {
                                e.printStackTrace();
                            }
                        } finally {
                            closeBroker(broker);
                        }
                    };
                    getRuntime().addShutdownHook(new Thread(hook));
                    String url = SOCKET;
                    url += url.contains("?") ? "&" : "?";
                    url += "jms.redeliveryPolicy.maximumRedeliveries=1&jms.redeliveryPolicy.initialRedeliveryDelay=0";
                    factory = new ActiveMQConnectionFactory(url);
                }
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
