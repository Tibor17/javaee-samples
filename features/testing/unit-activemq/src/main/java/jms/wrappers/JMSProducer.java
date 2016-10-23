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
package jms.wrappers;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;

import javax.jms.*;

import static java.util.Optional.empty;

public class JMSProducer<T extends Destination> extends BaseJMS {

    public static JMSProducer<Topic> createProducerOnTopic(String uri, String connectionId) throws JMSException {
        return new JMSProducer<>(uri, new ActiveMQTopic(connectionId));
    }

    public static JMSProducer<Topic> createProducerOnTopic(ConnectionFactory connectionFactory, String connectionId)
            throws JMSException {
        return new JMSProducer<>(connectionFactory, new ActiveMQTopic(connectionId));
    }

    public static <T extends Topic> JMSProducer<T> createProducerOnTopic(String uri, T topic) throws JMSException {
        return new JMSProducer<>(uri, topic);
    }

    public static <T extends Topic> JMSProducer<T> createProducerOnTopic(ConnectionFactory connectionFactory, T topic)
            throws JMSException {
        return new JMSProducer<>(connectionFactory, topic);
    }

    public static <T extends Queue> JMSProducer<T> createProducerOnQueue(String uri, T queue) throws JMSException {
        return new JMSProducer<>(uri, queue);
    }

    public static <T extends Queue> JMSProducer<T> createProducerOnQueue(ConnectionFactory connectionFactory, T queue)
            throws JMSException {
        return new JMSProducer<>(connectionFactory, queue);
    }

    public static JMSProducer<Queue> createProducerOnQueue(String uri, String connectionId) throws JMSException {
        return new JMSProducer<>(uri, new ActiveMQQueue(connectionId));
    }

    public static JMSProducer<Queue> createProducerOnQueue(ConnectionFactory connectionFactory, String connectionId)
            throws JMSException {
        return new JMSProducer<>(connectionFactory, new ActiveMQQueue(connectionId));
    }

    private final T destination;
    private final MessageProducer producer;

    public JMSProducer(ConnectionFactory connectionFactory, T destination) throws JMSException {
        super(connectionFactory, empty());
        this.destination = destination;
        producer = getSession().createProducer(destination);
    }

    public JMSProducer(String uri, T destination) throws JMSException {
        super(uri, empty());
        this.destination = destination;
        producer = getSession().createProducer(destination);
    }

    public T getDestination() {
        return destination;
    }

    public MessageProducer getMessageProducer() {
        return producer;
    }
}
