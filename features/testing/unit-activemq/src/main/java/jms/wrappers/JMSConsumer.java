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
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class JMSConsumer extends BaseJMS {
    public static JMSConsumer createConsumerOnTopic(String uri, String clientId, Optional<MessageListener> onMessage) throws JMSException {
        return new JMSConsumer(uri, new ActiveMQTopic(clientId), onMessage);
    }
    public static JMSConsumer createConsumerOnTopic(ConnectionFactory connectionFactory, String clientId, Optional<MessageListener> onMessage) throws JMSException {
        return new JMSConsumer(connectionFactory, new ActiveMQTopic(clientId), onMessage);
    }

    public static JMSConsumer createConsumerOnQueue(String uri, String clientId, Optional<MessageListener> onMessage) throws JMSException {
        return new JMSConsumer(uri, new ActiveMQQueue(clientId), onMessage);
    }

    public static JMSConsumer createConsumerOnQueue(ConnectionFactory connectionFactory, String clientId, Optional<MessageListener> onMessage) throws JMSException {
        return new JMSConsumer(connectionFactory, new ActiveMQQueue(clientId), onMessage);
    }

    private final String connectionId;
    private final Destination destination;
    private final MessageConsumer consumer;

    public JMSConsumer(ConnectionFactory connectionFactory, Topic topic, Optional<MessageListener> onMessage) throws JMSException {
        super(connectionFactory, ofNullable(topic.getTopicName()));
        destination = topic;
        connectionId = topic.getTopicName();
        consumer = getSession().createDurableSubscriber(topic, connectionId);
        if (onMessage.isPresent()) consumer.setMessageListener(onMessage.get());
        getConnection().start();
    }

    public JMSConsumer(String uri, Topic topic, Optional<MessageListener> onMessage) throws JMSException {
        super(uri, ofNullable(topic.getTopicName()));
        destination = topic;
        connectionId = topic.getTopicName();
        consumer = getSession().createDurableSubscriber(topic, connectionId);
        if (onMessage.isPresent()) consumer.setMessageListener(onMessage.get());
        getConnection().start();
    }

    public JMSConsumer(ConnectionFactory connectionFactory, Queue queue, Optional<MessageListener> onMessage) throws JMSException {
        super(connectionFactory, ofNullable(queue.getQueueName()));
        destination = queue;
        connectionId = queue.getQueueName();
        consumer = getSession().createConsumer(queue);
        if (onMessage.isPresent()) consumer.setMessageListener(onMessage.get());
        getConnection().start();
    }

    public JMSConsumer(String uri, Queue queue, Optional<MessageListener> onMessage) throws JMSException {
        super(uri, ofNullable(queue.getQueueName()));
        destination = queue;
        connectionId = queue.getQueueName();
        consumer = getSession().createConsumer(queue);
        if (onMessage.isPresent()) consumer.setMessageListener(onMessage.get());
        getConnection().start();
    }

    public Destination getDestination() {
        return destination;
    }

    public MessageConsumer getMessageConsumer() {
        return consumer;
    }
}
